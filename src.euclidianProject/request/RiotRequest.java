package request;

import java.util.Iterator;
import java.util.Set;

import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.league.dto.LeaguePosition;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;
import util.Ressources;

public class RiotRequest {

  private RiotRequest() {
  }

  public static String getSoloqRank(long summonerID) throws RiotApiException {

    Set<LeaguePosition> listLeague = Ressources.getRiotApi().getLeaguePositionsBySummonerId(Platform.EUW, summonerID);

    Iterator<LeaguePosition> gettableList = listLeague.iterator();

    String ligue = "Unranked";
    String rank = "";

    while (gettableList.hasNext()) {
      LeaguePosition leaguePosition = gettableList.next();

      System.out.println(leaguePosition.getTier());
      System.out.println(leaguePosition.getRank());
    }

    return ligue + " " + rank;
  }

  public static String getActualGameStatus(Summoner summoner) {

    CurrentGameInfo currentGameInfo;
    try {
      currentGameInfo = Ressources.getRiotApi().getActiveGameBySummoner(Platform.EUW, summoner.getId());
    } catch (RiotApiException e) {
      return "Pas en game";
    }

    String gameStatus = currentGameInfo.getGameMode();

    gameStatus += " (" + currentGameInfo.getGameType() + ") ";

    double minutesOfGames = (currentGameInfo.getGameLength()) / 60.0;
    String[] stringMinutesSecondes = Double.toString(minutesOfGames).split("\\.");
    int minutesGameLength = Integer.parseInt(stringMinutesSecondes[0]);
    int secondesGameLength = (int) (Double.parseDouble("0." + stringMinutesSecondes[1]) * 60.0);

    gameStatus += "(" + minutesGameLength + "m " + secondesGameLength + "s)";

    return gameStatus;
  }	
}