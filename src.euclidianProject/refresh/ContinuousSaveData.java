package refresh;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import main.Main;

public class ContinuousSaveData implements Runnable {
  
  private static boolean running;
  
  @Override
  public void run() {
    setRunning(true);
    try {
      Main.saveDataTxt();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    setRunning(false);
  }

  public static boolean isRunning() {
    return running;
  }

  public static void setRunning(boolean running) {
    ContinuousSaveData.running = running;
  }
}
