package ch.euclidian.main.util;

import java.util.List;
import ch.euclidian.main.model.Champion;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.util.request.RiotRequest;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameParticipant;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;

public class MessageBuilderRequestUtil {
  
  private MessageBuilderRequestUtil() {
    //Hide default public constructor
  }
  
  public static void createTeamData1Summoner(Summoner summoner, List<CurrentGameParticipant> blueTeam, StringBuilder blueTeamString,
      StringBuilder blueTeamRankString, StringBuilder blueTeamWinRateLastMonth) {
    for(int i = 0; i < blueTeam.size(); i++) {
      Champion champion = null;
      champion = Ressources.getChampionDataById(blueTeam.get(i).getChampionId());

      String rank = RiotRequest.getSoloqRank(blueTeam.get(i).getSummonerId()).toString();

      if(summoner.getName().equals(blueTeam.get(i).getSummonerName())) {
        blueTeamString.append(
            champion.getDisplayName() + " | __**" + NameConversion.convertStringToTinyString(blueTeam.get(i).getSummonerName()) + "**__" + "\n");
      } else {
        blueTeamString
            .append(champion.getDisplayName() + " | " + NameConversion.convertStringToTinyString(blueTeam.get(i).getSummonerName()) + "\n");
      }

      blueTeamRankString.append(rank + "\n");

      blueTeamWinRateLastMonth.append(RiotRequest.getMasterysScore(blueTeam.get(i).getSummonerId(), blueTeam.get(i).getChampionId()) + " | "
          + RiotRequest.getMood(blueTeam.get(i).getSummonerId()) + "\n");
    }
  }

  public static void getTeamPlayer(CurrentGameInfo match, int blueTeamID, List<CurrentGameParticipant> blueTeam,
      List<CurrentGameParticipant> redTeam) {
    for(int i = 0; i < match.getParticipants().size(); i++) {
      if(match.getParticipants().get(i).getTeamId() == blueTeamID) {
        blueTeam.add(match.getParticipants().get(i));
      } else {
        redTeam.add(match.getParticipants().get(i));
      }
    }
  }

  
  public static void createTeamDataMultipleSummoner(List<CurrentGameParticipant> blueTeam, List<String> listIdPlayers,
      StringBuilder blueTeamString, StringBuilder blueTeamRankString, StringBuilder blueTeamWinrateString) {
    for(int i = 0; i < blueTeam.size(); i++) {
      Champion champion = null;
      champion = Ressources.getChampionDataById(blueTeam.get(i).getChampionId());

      String rank = RiotRequest.getSoloqRank(blueTeam.get(i).getSummonerId()).toString();

      if(listIdPlayers.contains(blueTeam.get(i).getSummonerId())) {
        blueTeamString.append(
            champion.getDisplayName() + " | __**" + NameConversion.convertStringToTinyString(blueTeam.get(i).getSummonerName()) + "**__" + "\n");
      } else {
        blueTeamString
            .append(champion.getDisplayName() + " | " + NameConversion.convertStringToTinyString(blueTeam.get(i).getSummonerName()) + "\n");
      }

      blueTeamRankString.append(rank + "\n");

      blueTeamWinrateString.append(RiotRequest.getMasterysScore(blueTeam.get(i).getSummonerId(), blueTeam.get(i).getChampionId()) + " | "
          + RiotRequest.getMood(blueTeam.get(i).getSummonerId()) + "\n");
    }
  }

  public static void createTitle(List<Player> players, CurrentGameInfo currentGameInfo, StringBuilder title) {
    title.append("Info sur la partie de");

    for(int i = 0; i < players.size(); i++) {
      if(i + 1 == players.size()) {
        title.append(" et de " + players.get(i).getDiscordUser().getName());
      } else if(i + 2 == players.size()) {
        title.append(" " + players.get(i).getDiscordUser().getName());
      } else {
        title.append(" " + players.get(i).getDiscordUser().getName() + ",");
      }
    }

    title.append(" : " + NameConversion.convertGameQueueIdToString(currentGameInfo.getGameQueueConfigId()));
  }
}
