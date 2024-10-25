package config;

import android.util.Log;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
public class CustomAnalytics implements AnalyticsListener {

  @Override
  public void onMediaItemTransition(EventTime eventTime, @Nullable MediaItem mediaItem,
      int reason) {
    Log.i("MediaItemTransition", "Playing new video: ");
    if (mediaItem != null) {
      String videoUrl = mediaItem.playbackProperties.uri.toString();
      String videoInfo = mediaItem.mediaMetadata.toString();
      Log.i("MediaItemTransition", "Playing new video: " + videoInfo);
    }
  }
}
