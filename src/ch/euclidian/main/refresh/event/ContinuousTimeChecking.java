package ch.euclidian.main.refresh.event;

import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.euclidian.main.model.BotStatus;

public class ContinuousTimeChecking extends TimerTask{

  private static DateTime nextTimePanelRefresh;

  private static DateTime nextTimeSaveData;

  private static DateTime nextTimeSendReport;

  private static DateTime nextTimeStatusRefresh;

  private static int nbProcs = Runtime.getRuntime().availableProcessors();

  private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(nbProcs, nbProcs, 30,
      TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

  private static Logger logger = LoggerFactory.getLogger(ContinuousTimeChecking.class);

  @Override
  public void run() {

    if(nextTimeSendReport.isBeforeNow()) {
      logger.info("Launch Report");
      setNextTimeSendReport(nextTimeSendReport.plusWeeks(1));
      if(!ContinuousKeepData.isRunning()) { 
        threadPoolExecutor.submit(new ContinuousKeepData());
        ContinuousStatusRefresh.setStatus(BotStatus.REPORT_CONSTRUCTION);
      }
    }

    if(nextTimePanelRefresh.isBeforeNow()) {
      logger.info("Launch panel");
      setNextTimePanelRefresh(nextTimePanelRefresh.plusMinutes(3));
      if(!ContinuousPanelRefresh.isRunning()) {
        threadPoolExecutor.submit(new ContinuousPanelRefresh());
        ContinuousStatusRefresh.setStatus(BotStatus.PANEL_REFRESH);
      }
    }

    if(nextTimeSaveData.isBeforeNow()) {
      logger.info("Launch Save");
      setNextTimeSaveData(nextTimeSaveData.plusMinutes(10));
      if(!ContinuousSaveData.isRunning()) {
        threadPoolExecutor.submit(new ContinuousSaveData());
        ContinuousStatusRefresh.setStatus(BotStatus.SAVE_DATA);
      }
    }

    if(nextTimeStatusRefresh.isBeforeNow()) {
      logger.info("Launch Status refresh");
      setNextTimeStatusRefresh(nextTimeStatusRefresh.plusSeconds(30));
      if(!ContinuousStatusRefresh.isRunning()) {
        threadPoolExecutor.submit(new ContinuousStatusRefresh());
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
    threadPoolExecutor.shutdownNow();
  }

  public static DateTime getNextTimeStatusRefresh() {
    return nextTimeStatusRefresh;
  }

  public static void setNextTimeStatusRefresh(DateTime nextTimeStatusRefresh) {
    ContinuousTimeChecking.nextTimeStatusRefresh = nextTimeStatusRefresh;
  }

}
