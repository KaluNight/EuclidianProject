package ch.euclidian.main.util.request;

import java.util.Iterator;
import java.util.Set;

import ch.euclidian.main.util.NameConversion;
import ch.euclidian.main.util.Ressources;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.league.dto.LeaguePosition;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.constant.Platform;


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

      if(leaguePosition.getQueueType().equals("RANKED_SOLO_5x5")) {
        ligue = leaguePosition.getRank();
        rank = leaguePosition.getTier();
        return rank + " " + ligue + " (" + leaguePosition.getLeaguePoints() + " LP)";
      }
    }

    return ligue;
  }

  public static String getActualGameStatus(CurrentGameInfo currentGameInfo) {

    if(currentGameInfo == null) {
      return "Pas en game";
    }
    
    String gameStatus = NameConversion.convertGameQueueIdToString(currentGameInfo.getGameQueueConfigId()) + " ";

    double minutesOfGames = (currentGameInfo.getGameLength() + 180.0) / 60.0;
    String[] stringMinutesSecondes = Double.toString(minutesOfGames).split("\\.");
    int minutesGameLength = Integer.parseInt(stringMinutesSecondes[0]);
    int secondesGameLength = (int) (Double.parseDouble("0." + stringMinutesSecondes[1]) * 60.0);

    gameStatus += "(" + minutesGameLength + "m " + secondesGameLength + "s)";

    return gameStatus;
  }
}