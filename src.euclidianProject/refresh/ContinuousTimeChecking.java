package refresh;

import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContinuousTimeChecking extends TimerTask{

  private static DateTime nextTimePanelRefresh;

  private static DateTime nextTimeSaveData;

  private static DateTime nextTimeSendReport;

  private static int nbProcs = Runtime.getRuntime().availableProcessors();

  private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(nbProcs, nbProcs, 1000,
      TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

  Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void run() {

    if(nextTimeSendReport.isBeforeNow()) {
      logger.info("Lanche Report");
      setNextTimeSendReport(nextTimeSendReport.plusWeeks(1));
      if(!ContinuousKeepData.isRunning()) { 
        threadPoolExecutor.submit(new ContinuousKeepData());
      }
    }

    if(nextTimePanelRefresh.isBeforeNow()) {
      logger.info("Lanche panel");
      setNextTimePanelRefresh(nextTimePanelRefresh.plusMinutes(3));
      if(!ContinuousPanelRefresh.isRunning()) {
        threadPoolExecutor.submit(new ContinuousPanelRefresh());
      }
    }

    if(nextTimeSaveData.isBeforeNow()) {
      logger.info("Lanche Save");
      setNextTimeSaveData(nextTimeSaveData.plusMinutes(10));
      if(!ContinuousSaveData.isRunning()) {
        threadPoolExecutor.submit(new ContinuousSaveData());
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
  
  public static void shutdownThreadPool() {
    threadPoolExecutor.shutdown();
  }

}
