package util;

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
}
