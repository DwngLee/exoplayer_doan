package service;

import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PowerConsumptionLogger {

  private String fileName;
  private static File logFile;

  public static void init(File directory, String fileName) {
    logFile = new File(directory, fileName);
  }

  public static void PowerConsumptionLogger(String message) {
    logToFile(message);
  }

  private static void logToFile(String message) {
    if (logFile == null) {
      Log.e("DataLogger", "Log file not initialized. Call init() first.");
      return;
    }

    try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
      out.println(message);
    } catch (IOException e) {
      Log.e("DataLogger", "Error writing to log file", e);
    }
  }
}
