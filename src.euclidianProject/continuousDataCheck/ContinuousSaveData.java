package continuousDataCheck;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.Main;

public class ContinuousSaveData extends Thread{

  Logger logger = LoggerFactory.getLogger(getClass());
  
  @Override
  public void run() {
    logger.info("Save data");
    try {
      Main.saveDataTxt();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
}
