package continuousDataCheck;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import main.Main;

public class ContinuousSaveData extends Thread{

  @Override
  public void run() {
    try {
      Main.saveDataTxt();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
}
