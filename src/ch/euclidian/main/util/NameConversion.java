package ch.euclidian.main.util;

import java.util.ArrayList;
import java.util.List;
import ch.euclidian.main.model.Player;

public class NameConversion {

  private NameConversion() {}

  public static String convertStringToTinyString(String stringToConvert) {
    if(stringToConvert.length() > 14) {
      return stringToConvert.substring(0, 12) + "..";
    }
    return stringToConvert;
  }

  public static String convertGameModeToString(String stringToConvert) {

    if(stringToConvert.equals("CLASSIC")) {
      return "Faille de l'invocateur";
    } else if(stringToConvert.equals("GAMEMOEDEX")) {
      return "Mode de jeu en rotation";
    } else {
      return stringToConvert;
    }
  }

  public static String convertGameQueueIdToString(int id) {
    switch(id) {
      case 0:
        return "Partie personnalisée";
      case 400:
        return "Normal Draft | Faille de l'invocateur";
      case 420:
        return "Classé Solo/Duo | Faille de l'invocateur";
      case 430:
        return "Normal Draft | Faille de l'invocateur";
      case 440:
        return "Classé Flex | Faille de l'invocateur";
      case 450:
        return "ARAM | Abîme Hurlant";
      case 470:
        return "Classé 3v3 | Forêt torturée";
      case 700:
        return "Clash | Faille de l'invocateur";
      case 800:
      case 810:
      case 820:
        return "Coop vs IA | Forêt torturée";
      case 830:
      case 840:
      case 850:
        return "Coop vs IA | Faille de l'invocateur";
      case 900:
        return "ARURF | Faille de l'invocateur";
      case 1200:
        return "Raid du Nexus | Faille de l'invocateur";
      default:
        return "Mode temporaire";
    }
  }

  public static String convertGameTypeToString(String stringToConvert) {

    if(stringToConvert.equals("MATCHED_GAME")) {
      return "Matchmaking";
    } else {
      return stringToConvert;
    }
  }

  public static List<String> getListNameOfPlayers(List<Player> players) {
    List<String> playersName = new ArrayList<>();

    for(int j = 0; j < players.size(); j++) {
      String name = "";
      if(players.get(j).isMentionnable()) {
        name = players.get(j).getDiscordUser().getAsMention();
      } else {
        name = players.get(j).getDiscordUser().getName();
      }
      playersName.add(name);
    }
    return playersName;
  }
}
