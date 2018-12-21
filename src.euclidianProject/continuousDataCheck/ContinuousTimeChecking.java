package continuousDataCheck;

import java.util.TimerTask;

import org.joda.time.DateTime;

public class ContinuousTimeChecking extends TimerTask{
  
  private static DateTime nextTimePanelRefresh;

  private static DateTime nextTimeSaveData;
  
  private static DateTime nextTimeSendReport;
  
  @Override
  public void run() {
    
    if(nextTimeSendReport.isBeforeNow()) {
      setNextTimeSendReport(nextTimeSendReport.plusWeeks(1));
      new ContinuousKeepData().start();
    }
    
    if(nextTimePanelRefresh.isBeforeNow()) {
      setNextTimePanelRefresh(nextTimePanelRefresh.plusMinutes(3));
      new ContinuousPanelRefresh().start();
    }
    
    if(nextTimeSaveData.isBeforeNow()) {
      setNextTimeSaveData(nextTimeSaveData.plusMinutes(10));
      new ContinuousSaveData().start();
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
