package util;

import main.Main;

public class LogHelper {

  private LogHelper() {
  }

  public static void logSender(String str) {
    Main.getLogBot().sendMessage(str).queue();
  }

  public static void logSenderDirectly(String str) {
	  Main.getLogBot().sendMessage(str).complete();
  }
}