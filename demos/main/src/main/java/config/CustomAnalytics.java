package config;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.LoadEventInfo;
import com.google.android.exoplayer2.source.MediaLoadData;
import com.google.android.exoplayer2.util.AudioQuality;
import com.google.android.exoplayer2.util.AudioSegments;
import com.google.android.exoplayer2.util.StallingTimeInfo;
import com.google.android.exoplayer2.util.VideoQuality;
import com.google.android.exoplayer2.util.VideoSegments;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import models.VideoPlaybackData;
import service.FileService;

public class CustomAnalytics implements AnalyticsListener, Runnable {
  private Context context;
  private BatteryManager batteryManager;
  private Handler handler;
  private VideoSegments I13;
  private StallingTimeInfo I23;
  private AudioSegments I11;
  private long bufferStartTime = -1;
  private boolean isLastVideoEnded = false;
  private static String dataOutput = "";
  private static final String POWER_CONSUMPTION_FILE_NAME = "power_consumption_";
  private static final String VIDEO_PLAYBACK_DATA_FILE_NAME = "playback_data_";
  private static final String ENERGY_TAG = "PowerMonitor";

  public CustomAnalytics(Context context, BatteryManager batteryManager, Handler handler) {
    this.context = context;
    this.batteryManager = batteryManager;
    this.handler = handler;
    initData();
  }

  public void initData(){
    bufferStartTime = -1;
    List<VideoQuality> videoQualityList = new ArrayList<>();
    List<AudioQuality> audioQualityList = new ArrayList<>();
    List<List<Long>> stallingList = new ArrayList<>();
    this.I13 = new VideoSegments(videoQualityList, 42);
    this.I23 = new StallingTimeInfo(stallingList, 42);
    this.I11 = new AudioSegments(audioQualityList, 42);
  }

  @Override
  public void onLoadCompleted(EventTime eventTime, LoadEventInfo loadEventInfo,
      MediaLoadData mediaLoadData) {
    long mediaStartTimeMs = mediaLoadData.mediaStartTimeMs;
    long mediaEndTimeMs = mediaLoadData.mediaEndTimeMs;
    long segmentDuration = mediaEndTimeMs - mediaStartTimeMs;
    Format trackFormat = mediaLoadData.trackFormat; // Lấy format video
    int trackType = mediaLoadData.trackType; // Lấy track type
    long timeStamp = mediaStartTimeMs < 0 ? 0 : mediaStartTimeMs;

    if(trackFormat != null && trackType == C.TRACK_TYPE_VIDEO && segmentDuration > 0){ //Tracktype = 2: Video, tìm hiểu thêm các enum C.TrackType ở đây: https://developer.android.com/reference/androidx/media3/common/C#TRACK_TYPE_UNKNOWN()
      VideoQuality videoQuality = new VideoQuality(
          trackFormat.bitrate/1024,
          "h264", //Default codec in itu p1203
          segmentDuration/1000,
          trackFormat.frameRate,
          trackFormat.width + "x" + trackFormat.height,
          timeStamp / 1000
      );
      I13.getVideoQualityList().add(videoQuality);
    }else if(trackFormat != null && trackType == C.TRACK_TYPE_AUDIO && segmentDuration > 0){
      AudioQuality audioQuality = new AudioQuality(
          trackFormat.bitrate/1024,
          "aaclc",
          segmentDuration/1000,
          timeStamp/1000
      );
      I11.getSegments().add(audioQuality);
    }
  }

  @Override
  public void onPlaybackStateChanged(EventTime eventTime, int state) {
    if (state == Player.STATE_BUFFERING) { //Tìm thời điểm xảy ra rebuffering
      if (bufferStartTime == -1) {
        bufferStartTime = System.currentTimeMillis();
      }
    } else if (state == Player.STATE_READY) {
      if (bufferStartTime != -1) { //Khi hết rebuffering thì sẽ tính duration
        long bufferEndTime = System.currentTimeMillis();
        Long stallDuration = (bufferEndTime - bufferStartTime)/1000;
        //#TODO: viết lại cho clear hơn
        long stallingAt = eventTime.currentPlaybackPositionMs/1000;
        if(stallingAt >= 0 && stallDuration > 0){
          I23.getStalling().add(Arrays.asList(stallingAt, stallDuration));
          bufferStartTime = -1;
        }

      }
    }
    // Kiểm tra khi video kết thúc
    if (state == Player.STATE_ENDED) {
      isLastVideoEnded = true;
      String fileName = FileService.generateFileName();
      savePowerConsumptionData(fileName);
      saveVideoPlaybackData(fileName);
    }
  }

  @Override
  public void onMediaItemTransition(EventTime eventTime, @Nullable MediaItem mediaItem,
      int reason) {
    if (mediaItem != null && !isLastVideoEnded) {
      String fileName = FileService.generateFileName();
      savePowerConsumptionData(fileName);
      saveVideoPlaybackData(fileName);
    }
  }

  private void savePowerConsumptionData(String fileName) {
    FileService.saveDataToFile(context, "power_consumption", dataOutput, POWER_CONSUMPTION_FILE_NAME + fileName + ".csv");
    dataOutput = "";
  }

  private void saveVideoPlaybackData(String fileName) {
    FileService.saveDataToFile(context, "playback_data", getVideoPlaybackData().toJson(), VIDEO_PLAYBACK_DATA_FILE_NAME + fileName + ".json");
    initData();
  }

  private String generateFileName(){
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    return timeStamp;
  }

  private void logPowerConsumption() {
    if (batteryManager != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        long energyCounter = batteryManager.getLongProperty(
            BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER); //Năng lượng còn lại trong pin
        int capacity = batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY); //Dung lượng pin còn lại theo %
        long currentNow = batteryManager.getLongProperty(
            BatteryManager.BATTERY_PROPERTY_CURRENT_NOW); //Dòng điện hiện tại pin đang cung cấp hoặc nhận. Giá trị âm tức là đang sạc (nhận)
        long currentAverage = batteryManager.getLongProperty(
            BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE); //Dòng điện trung bình pin đã cung cấp trong 1 time
        long chargeCounter =
            batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                / 1000000; //Dung lượng còn lại của pin
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String data = capacity + "," + currentNow + "," + timeStamp + "\n";
        dataOutput += data;
        Log.i(ENERGY_TAG, "Remaining battery capacity = " + capacity + " %" + ", "
            + "Instantaneous battery current = " + currentNow + " µA" + ", "
            + "Average battery current = " + currentAverage + " µA" + ", "
            + "Remaining battery capacity = " + chargeCounter + " µAh");
      }
    }
  }

  private VideoPlaybackData getVideoPlaybackData() {
    return new VideoPlaybackData(I13, I23, I11);
  }

  @Override
  public void run() {
    logPowerConsumption();
    handler.postDelayed(this, 1000);
  }
}
