package ch.euclidian.main.util;

import java.util.ArrayList;
import java.util.List;

import ch.euclidian.main.model.Player;

public class NameConversion {

  private NameConversion() {
  }
  
  public static String convertGameModeToString(String stringToConvert) {
    
    if(stringToConvert.equals("CLASSIC")) {
       return "Faille de l'invocateur";
    }else if(stringToConvert.equals("GAMEMOEDEX")) {
      return "Mode de jeu en rotation";
    }else {
      return stringToConvert;
    }
  }
  
  public static String convertGameTypeToString(String stringToConvert) {
    
    if(stringToConvert.equals("MATCHED_GAME")) {
      return "Matchmaking";
    }else {
      return stringToConvert;
    }
  }
  
  public static List<String> getListNameOfPlayers(List<Player> players) {
    List<String> playersName = new ArrayList<>();
    
    for(int j = 0; j < players.size(); j++) {
      String name = "";
      if(players.get(j).isMentionnable()) {
        name = players.get(j).getDiscordUser().getAsMention();
      }else {
        name = players.get(j).getDiscordUser().getName();
      }
      playersName.add(name);
    }
    return playersName;
  }
}
