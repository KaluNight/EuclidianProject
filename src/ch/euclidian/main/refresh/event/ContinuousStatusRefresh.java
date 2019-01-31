package ch.euclidian.main.refresh.event;

import java.util.Random;
import ch.euclidian.main.Main;
import ch.euclidian.main.model.BotStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;

public class ContinuousStatusRefresh implements Runnable {

  private static final String[] LIST_QUOTE_IDLE = {"one-shot un adc", "flash 6 fois de suite", "endormir un jungler égaré",
      "courir derrière des papillons", "chasser Lux", "danser avec Neeko"};

  private static final String[] LIST_QUOTE_SAVE_DATA = {"sauvegarder des donnés importantes", "triée des feuilles"};

  private static final String[] LIST_QUOTE_PANEL_REFRESH = {"analyser des parties", "créer des cartes informatives"};

  private static final String[] LIST_QUOTE_REPORT_CONTRUCTION =
      {"analyser les parties de la semaines", "retourner dans le passés", "se balader dans les archives"};

  private static final Random random = new Random();

  private static BotStatus status = BotStatus.IDLE;

  private static boolean running;

  @Override
  public void run() {
    try {
      setRunning(true);
      String quote = "";

      if(ContinuousKeepData.isRunning()) {
        quote = LIST_QUOTE_REPORT_CONTRUCTION[randInt(0, LIST_QUOTE_REPORT_CONTRUCTION.length)];
      } else {
        switch(status) {
          case IDLE:
            quote = LIST_QUOTE_IDLE[randInt(0, LIST_QUOTE_IDLE.length)];
            break;
          case PANEL_REFRESH:
            quote = LIST_QUOTE_PANEL_REFRESH[randInt(0, LIST_QUOTE_PANEL_REFRESH.length)];
            break;
          case REPORT_CONSTRUCTION:
            quote = LIST_QUOTE_REPORT_CONTRUCTION[randInt(0, LIST_QUOTE_REPORT_CONTRUCTION.length)];
            break;
          case SAVE_DATA:
            quote = LIST_QUOTE_SAVE_DATA[randInt(0, LIST_QUOTE_SAVE_DATA.length)];
            break;
        }
      }
      setStatus(BotStatus.IDLE);

      Main.getJda().getPresence().setGame(Game.of(GameType.DEFAULT, quote));

    } finally {
      setRunning(false);
    }
  }

  public static int randInt(int min, int max) {
    return random.nextInt(max - min) + min;
  }

  public static BotStatus getStatus() {
    return status;
  }

  public static void setStatus(BotStatus status) {
    ContinuousStatusRefresh.status = status;
  }

  public static boolean isRunning() {
    return running;
  }

  public static void setRunning(boolean running) {
    ContinuousStatusRefresh.running = running;
  }
}
