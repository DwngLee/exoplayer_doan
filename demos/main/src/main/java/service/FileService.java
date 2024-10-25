package service;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
//#TODO: Co về 1 hàm xử lý file
public class FileService {
  public static void saveJsonToFile(Context context, String jsonString, String fileName) {
    try {
      File dir = new File(context.getFilesDir(), "playback_data");
      Log.e("File dir:::", dir.getPath());
      if (!dir.exists()) {
        dir.mkdir();
      }

      File file = new File(dir, fileName);
      FileWriter writer = new FileWriter(file);
      writer.write(jsonString);
      writer.close();
      System.out.println("JSON data saved to " + file.getAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String generateFileName() {
    String timestamp = LocalDateTime.now().toString();
    return "playback_data_" + timestamp + ".json";
  }
}
