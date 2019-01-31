package ch.euclidian.main.refresh.event;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import ch.euclidian.main.Main;

public class ContinuousSaveData implements Runnable {

  private static boolean running;

  @Override
  public void run() {
    try {
      setRunning(true);
      try {
        Main.saveDataTxt();
      } catch(FileNotFoundException e) {
        e.printStackTrace();
      } catch(UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    } finally {
      setRunning(false);
    }
  }

  public static boolean isRunning() {
    return running;
  }

  public static void setRunning(boolean running) {
    ContinuousSaveData.running = running;
  }
}
