package ch.euclidian.main.util.request;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.euclidian.main.util.NameConversion;
import ch.euclidian.main.util.Ressources;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.champion_mastery.dto.ChampionMastery;
import net.rithms.riot.api.endpoints.league.dto.LeaguePosition;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.match.dto.MatchList;
import net.rithms.riot.api.endpoints.match.dto.MatchReference;
import net.rithms.riot.api.endpoints.match.dto.Participant;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;


public class RiotRequest {

  private static final Logger logger = LoggerFactory.getLogger(RiotRequest.class);

  private static final DecimalFormat df = new DecimalFormat("###.##");

  private static final int MAX_GAME_FOR_WINRATE = 20 - 1;

  private RiotRequest() {
  }

  public static String getSoloqRank(String summonerID) {

    Set<LeaguePosition> listLeague;
    try {
      listLeague = Ressources.getRiotApi().getLeaguePositionsBySummonerId(Platform.EUW, summonerID);
    } catch (RiotApiException e) {
      logger.warn("Impossible d'obtenir le rank de la personne : {}", e.getMessage(), e);
      return "Inconnu";
    }
    
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

  public static String getWinrateLast20Games(String summonerId) {
    DateTime actualTime = DateTime.now();

    Summoner summoner;
    try {
      summoner = Ressources.getRiotApi().getSummoner(Platform.EUW, summonerId);
    }catch (RiotApiException e) {
      logger.warn("Impossible d'obtenir le summoner : {}" , e.getMessage());
      return "Aucune donnés";
    }

    List<MatchReference> matchesReferences = new ArrayList<>();

    DateTime beginTime = actualTime.minusWeeks(1);

    MatchList matchList = null;

    try {
      matchList =  Ressources.getRiotApi().getMatchListByAccountId
          (Platform.EUW, summoner.getAccountId(), null, null, null, beginTime.getMillis(), actualTime.getMillis(), -1, -1);
    } catch (RiotApiException e) {
      logger.warn("Impossible d'obtenir la list de match : {}", e.getMessage());
    }
    
    if(matchList != null) {
      matchesReferences.addAll(matchList.getMatches());
    }

    if(matchesReferences.size() > MAX_GAME_FOR_WINRATE) {
      int size = matchesReferences.size(); 

      for(int i = size - 1; i > MAX_GAME_FOR_WINRATE; i--) {
        matchesReferences.remove(i);
      }
    }

    int nbrGames = 0;
    int nbrWin = 0;

    for(int i = 0; i < matchesReferences.size(); i++) {
      MatchReference matchReference = matchesReferences.get(i);

      Match match = null;
      try {
        match = Ressources.getRiotApi().getMatch(Platform.EUW, matchReference.getGameId());
      } catch (RiotApiException e) {
        logger.warn("Match ungetable from api : {}", e.getMessage());
      }

      if(match != null) {
        Participant participant = match.getParticipantByAccountId(summoner.getAccountId());

        if(participant != null && participant.getTimeline().getCreepsPerMinDeltas() != null) {

          String result = match.getTeamByTeamId(participant.getTeamId()).getWin();
          if(result.equalsIgnoreCase("Win") || result.equalsIgnoreCase("Fail")) {
            nbrGames++;
          }

          if(result.equalsIgnoreCase("Win")) {
            nbrWin++;
          }
        }
      }
    }

    if(nbrGames == 0) {
      return "Première game de ce mois";
    }else if(nbrWin == 0) {
      return "0% (" + nbrGames + " parties)";
    }

    return df.format((nbrWin / (double) nbrGames) * 100) + "% (" + nbrGames + " parties)";
  }
  
  public static String getMasterysScore(String summonerId, int championId) {
    ChampionMastery mastery = null;
    try {
      mastery = Ressources.getRiotApi().getChampionMasteriesBySummonerByChampion(Platform.EUW, summonerId, championId);
    } catch (RiotApiException e) {
      logger.warn("Impossible d'obtenir le score de mastery : {}", e.getMessage());
      return "?";
    }
    
    long points = mastery.getChampionPoints();
    if(points > 1000 && points < 1000000) {
      return points / 1000 + "K";
    }else if (points > 1000000) {
      return points / 1000000 + "M";
    }else {
      return Long.toString(points);
    }
  }
  
  public static String getMood(String summonerId) {
    DateTime actualTime = DateTime.now();

    Summoner summoner;
    try {
      summoner = Ressources.getRiotApi().getSummoner(Platform.EUW, summonerId);
    }catch (RiotApiException e) {
      logger.warn("Impossible d'obtenir le summoner : {}" , e.getMessage());
      return "Aucune donnés";
    }

    List<MatchReference> matchesReferences = new ArrayList<>();

    DateTime beginTime = actualTime.minusHours(3);

    MatchList matchList = null;

    try {
      matchList =  Ressources.getRiotApi().getMatchListByAccountId
          (Platform.EUW, summoner.getAccountId(), null, null, null, beginTime.getMillis(), actualTime.getMillis(), -1, -1);
    } catch (RiotApiException e) {
      logger.warn("Impossible d'obtenir la list de match : {}", e.getMessage());
    }
    
    if(matchList != null) {
      matchesReferences.addAll(matchList.getMatches());
    }
    
    int nbrGames = 0;
    int nbrWin = 0;
    
    for(int i = 0; i < matchesReferences.size(); i++) {
      MatchReference matchReference = matchesReferences.get(i);

      Match match = null;
      try {
        match = Ressources.getRiotApi().getMatch(Platform.EUW, matchReference.getGameId());
      } catch (RiotApiException e) {
        logger.warn("Match ungetable from api : {}", e.getMessage());
      }

      if(match != null) {
        Participant participant = match.getParticipantByAccountId(summoner.getAccountId());

        if(participant != null && participant.getTimeline().getCreepsPerMinDeltas() != null) {

          String result = match.getTeamByTeamId(participant.getTeamId()).getWin();
          if(result.equalsIgnoreCase("Win") || result.equalsIgnoreCase("Fail")) {
            nbrGames++;
          }

          if(result.equalsIgnoreCase("Win")) {
            nbrWin++;
          }
        }
      }
    }
    
    if(nbrGames == 0) {
      return "Inconnu";
    }else if(nbrWin == 0 && nbrGames >= 3) {
      return "Très mauvais";
    }
    
    double winrate = (nbrWin / (double) nbrGames) * 100;
  
    if(winrate == 100 && nbrGames >= 2) {
      return "Excellent";
    } else if(winrate < 50) {
      return "Mauvais";
    } else if (winrate > 50) {
      return "Bon";
    }
    
    return "Inconnu";
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
  