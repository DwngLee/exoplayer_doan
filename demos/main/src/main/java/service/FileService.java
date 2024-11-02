package service;

import android.content.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//#TODO: Co về 1 hàm xử lý file
public class FileService {
  public static void saveDataToFile(Context context, String folder, String data,  String fileName) {
    try {
      File dir = new File(context.getFilesDir(), folder);
      if (!dir.exists()) {
        dir.mkdir();
      }

      File file = new File(dir, fileName);
      FileWriter writer = new FileWriter(file);
      writer.write(data);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String generateFileName() {
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    return timeStamp;
  }
}
