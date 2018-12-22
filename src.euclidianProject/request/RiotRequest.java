package request;

import com.merakianalytics.orianna.types.core.league.LeaguePositions;
import com.merakianalytics.orianna.types.core.spectator.CurrentMatch;
import com.merakianalytics.orianna.types.core.summoner.Summoner;

import util.LogHelper;

public class RiotRequest {

  public static String getSoloqRank(long summonerID) {
    LeaguePositions list = LeaguePositions.forSummoner(Summoner.withId(summonerID).get()).get();

    String ligue = "Unranked";
    String rank = "";

    for(int i = 0; i < list.size(); i++) {

      System.out.println(list.get(i));

    }

    return ligue + " " + rank;
  }

  public static String getActualGameStatus(Summoner summoner) {

    LogHelper.logSender("In Riot request");

    CurrentMatch currentGameInfo = CurrentMatch.forSummoner(summoner).get();
    LogHelper.logSender("After Riot request");
    try {
      if(!currentGameInfo.exists()) {  //Get freeze here
        LogHelper.logSender("Before return");
        return "Pas en game";
      }
    }catch (Exception e) {
      LogHelper.logSender(e.getMessage());
    }

    String gameStatus = currentGameInfo.getMode().name();
    LogHelper.logSender("after return");

    gameStatus += " (" + currentGameInfo.getType().name() + ") ";

    double minutesOfGames = currentGameInfo.getDuration().getStandardSeconds() / 60.0;
    String[] stringMinutesSecondes = Double.toString(minutesOfGames).split("\\.");
    int minutesGameLength = Integer.parseInt(stringMinutesSecondes[0]);
    int secondesGameLength = (int) (Double.parseDouble("0." + stringMinutesSecondes[1]) * 60.0);

    gameStatus += "(" + minutesGameLength + "m " + secondesGameLength + "s)";

    LogHelper.logSender("Out Riot request");

    return gameStatus;
  }	
}