package continuousDataCheck;

import java.util.TimerTask;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContinuousTimeChecking extends TimerTask{

  private static DateTime nextTimePanelRefresh;

  private static DateTime nextTimeSaveData;

  private static DateTime nextTimeSendReport;

  Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void run() {

    if(nextTimeSendReport.isBeforeNow()) {
      logger.info("Lanche Report");
      setNextTimeSendReport(nextTimeSendReport.plusWeeks(1));
      if(!ContinuousKeepData.isRunning()) { 
        new ContinuousKeepData().start();
      }
    }

    if(nextTimePanelRefresh.isBeforeNow()) {
      logger.info("Lanche panel");
      setNextTimePanelRefresh(nextTimePanelRefresh.plusMinutes(3));
      if(!ContinuousPanelRefresh.isRunning()) {
        new ContinuousPanelRefresh().start();
      }
    }

    if(nextTimeSaveData.isBeforeNow()) {
      logger.info("Lanche Save");
      setNextTimeSaveData(nextTimeSaveData.plusMinutes(10));
      if(!ContinuousSaveData.isRunning()) {
        new ContinuousSaveData().start();
      }
    }
  }

  public static DateTime getNextTimePanelRefresh() {
    return nextTimePanelRefresh;
  }

  public static void setNextTimePanelRefresh(DateTime nextTimePanelRefresh) {
    ContinuousTimeChecking.nextTimePanelRefresh = nextTimePanelRefresh;
  }

  public static DateTime getNextTimeSaveData() {
    return nextTimeSaveData;
  }

  public static void setNextTimeSaveData(DateTime nextTimeSaveData) {
    ContinuousTimeChecking.nextTimeSaveData = nextTimeSaveData;
  }

  public static DateTime getNextTimeSendReport() {
    return nextTimeSendReport;
  }

  public static void setNextTimeSendReport(DateTime nextTimeSendReport) {
    ContinuousTimeChecking.nextTimeSendReport = nextTimeSendReport;
  }

}
