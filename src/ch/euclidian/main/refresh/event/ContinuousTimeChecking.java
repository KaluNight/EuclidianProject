package ch.euclidian.main.refresh.event;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.euclidian.main.model.BotStatus;

public class ContinuousTimeChecking extends TimerTask {

  private static DateTime nextTimePanelRefresh;

  private static DateTime nextTimeSaveData;

  private static DateTime nextTimeSendReport;

  private static DateTime nextTimeStatusRefresh;

  private static DateTime nextTimeCheckLive;

  private static LocalTime timeToTierSave = LocalTime.of(15, 48);
  private static boolean saveTierDone = false;

  private static int nbProcs = Runtime.getRuntime().availableProcessors();

  private static ThreadPoolExecutor threadPoolExecutor =
      new ThreadPoolExecutor(nbProcs, nbProcs, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

  private static Logger logger = LoggerFactory.getLogger(ContinuousTimeChecking.class);

  @Override
  public void run() {

    if(nextTimeSendReport.isBeforeNow()) {
      setNextTimeSendReport(nextTimeSendReport.plusWeeks(1));
      if(!ContinuousKeepData.isRunning()) {
        logger.info("Launch Report");
        threadPoolExecutor.submit(new ContinuousKeepData());
        ContinuousStatusRefresh.setStatus(BotStatus.REPORT_CONSTRUCTION);
      }
    }

    if(nextTimePanelRefresh.isBeforeNow()) {
      setNextTimePanelRefresh(nextTimePanelRefresh.plusMinutes(3));
      logger.info("Launch panel");
      if(!ContinuousPanelRefresh.isRunning()) {
        threadPoolExecutor.submit(new ContinuousPanelRefresh());
        ContinuousStatusRefresh.setStatus(BotStatus.PANEL_REFRESH);
      }
    }

    if(nextTimeSaveData.isBeforeNow()) {
      setNextTimeSaveData(nextTimeSaveData.plusMinutes(10));
      if(!ContinuousSaveData.isRunning()) {
        logger.info("Launch Save");
        threadPoolExecutor.submit(new ContinuousSaveData());
        ContinuousStatusRefresh.setStatus(BotStatus.SAVE_DATA);
      }
    }

    if(nextTimeStatusRefresh.isBeforeNow()) {
      setNextTimeStatusRefresh(nextTimeStatusRefresh.plusSeconds(30));
      if(!ContinuousStatusRefresh.isRunning()) {
        logger.info("Launch Status refresh");
        threadPoolExecutor.submit(new ContinuousStatusRefresh());
      }
    }

    if(nextTimeCheckLive.isBeforeNow()) {
      setNextTimeCheckLive(nextTimeCheckLive.plusMinutes(2));
      if(!ContinuousStreamOnlineChecking.isRunning()) {
        logger.info("Launch Stream Checking");
        threadPoolExecutor.submit(new ContinuousStreamOnlineChecking());
      }
    }

    if(LocalTime.now().truncatedTo(ChronoUnit.MINUTES).equals(timeToTierSave) && !ContinuousTierKeeping.isRunning() && !isSaveTierDone()) {
      setSaveTierDone(true);
      logger.info("Launch Tier Save");
      threadPoolExecutor.submit(new ContinuousTierKeeping());
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

  public static DateTime getNextTimeCheckLive() {
    return nextTimeCheckLive;
  }

  public static void setNextTimeCheckLive(DateTime nextTimeCheckLive) {
    ContinuousTimeChecking.nextTimeCheckLive = nextTimeCheckLive;
  }

  public static boolean isSaveTierDone() {
    return saveTierDone;
  }

  public static void setSaveTierDone(boolean saveTierDone) {
    ContinuousTimeChecking.saveTierDone = saveTierDone;
  }

}
