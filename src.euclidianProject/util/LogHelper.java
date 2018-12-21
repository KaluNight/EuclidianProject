package util;

import main.Main;

public class LogHelper {

  private LogHelper() {
  }

  public static void logSender(String str) {
    Main.getLogBot().sendMessage(str).queue();
  }

}
