package ch.euclidian.main.util.request;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.euclidian.main.model.Champion;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.model.Postulation;
import ch.euclidian.main.util.Ressources;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.User;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameParticipant;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;

public class MessageBuilderRequest {

  private static Logger logger = LoggerFactory.getLogger(MessageBuilderRequest.class);

  private MessageBuilderRequest() {
  }

  public static MessageEmbed createInfoCard1summoner(User user, Summoner summoner, CurrentGameInfo match) {

    EmbedBuilder message = new EmbedBuilder();

    message.setAuthor(user.getName(), null, user.getAvatarUrl());

    message.setTitle("Info sur la partie de " + user.getName());

    int blueTeamID = 0;

    for(int i = 0; i < match.getParticipants().size(); i++) {
      if(i == 0) {
        blueTeamID = match.getParticipants().get(i).getTeamId();
      }
    }

    ArrayList<CurrentGameParticipant> blueTeam = new ArrayList<>();
    ArrayList<CurrentGameParticipant> redTeam = new ArrayList<>();

    for(int i = 0; i < match.getParticipants().size(); i++) {
      if(match.getParticipants().get(i).getTeamId() == blueTeamID) {
        blueTeam.add(match.getParticipants().get(i));
      } else {
        redTeam.add(match.getParticipants().get(i));
      }
    }

    StringBuilder blueTeamString = new StringBuilder();
    StringBuilder blueTeamRankString = new StringBuilder();

    for(int i = 0; i < blueTeam.size(); i++) {
      Champion champion = null;
      champion = Ressources.getChampionDataById(blueTeam.get(i).getChampionId());

      String rank = null;
      try {
        rank = RiotRequest.getSoloqRank(blueTeam.get(i).getSummonerId());
      } catch (RiotApiException e) {
        logger.error(e.getMessage());
        return null;
      }

      if(summoner.getName().equals(blueTeam.get(i).getSummonerName())) {
        blueTeamString.append(champion.getName() + " | __**" + blueTeam.get(i).getSummonerName() + "**__" + "\n");
      }else {
        blueTeamString.append(champion.getName() + " | " + blueTeam.get(i).getSummonerName() + "\n");
      }

      blueTeamRankString.append(rank + "\n");
    }

    message.addField("Équipe Bleu", blueTeamString.toString(), true);
    message.addField("Grades", blueTeamRankString.toString(), true);

    StringBuilder redTeamString = new StringBuilder();
    StringBuilder redTeamRankString = new StringBuilder();

    for(int i = 0; i < redTeam.size(); i++) {
      Champion champion = null;
      champion = Ressources.getChampionDataById(redTeam.get(i).getChampionId());

      String rank = null;
      try {
        rank = RiotRequest.getSoloqRank(redTeam.get(i).getSummonerId());
      } catch (RiotApiException e) {
        logger.error(e.getMessage());
        return null;
      }

      if(summoner.getName().equals(redTeam.get(i).getSummonerName())) {
        redTeamString.append(champion.getName() + " | __**" + redTeam.get(i).getSummonerName() + "**__" + "\n");
      }else {
        redTeamString.append(champion.getName() + " | " + redTeam.get(i).getSummonerName() + "\n");
      }
      redTeamRankString.append(rank + "\n");
    }
    message.addBlankField(false);
    message.addField("Équipe Rouge", redTeamString.toString(), true);
    message.addField("Grades", redTeamRankString.toString(), true);

    message.setColor(Color.GREEN);

    return message.build();
  }

  public static MessageEmbed createInfoCardsMultipleSummoner(List<Player> players, CurrentGameInfo currentGameInfo) {

    EmbedBuilder message = new EmbedBuilder();

    StringBuilder title = new StringBuilder();

    title.append("Info sur la partie de");

    for(int i = 0; i < players.size(); i++) {
      if(i + 1 == players.size()) {
        title.append(" et de " + players.get(i).getDiscordUser().getName());
      }else if(i + 2 == players.size()) {
        title.append(" " + players.get(i).getDiscordUser().getName());
      }else {
        title.append(" " + players.get(i).getDiscordUser().getName() + ",");
      }
    }

    message.setTitle(title.toString());

    int blueTeamID = 0;

    for(int i = 0; i < currentGameInfo.getParticipants().size(); i++) {
      if(i == 0) {
        blueTeamID = currentGameInfo.getParticipants().get(i).getTeamId();
      }
    }

    ArrayList<CurrentGameParticipant> blueTeam = new ArrayList<>();
    ArrayList<CurrentGameParticipant> redTeam = new ArrayList<>();

    for(int i = 0; i < currentGameInfo.getParticipants().size(); i++) {
      if(currentGameInfo.getParticipants().get(i).getTeamId() == blueTeamID) {
        blueTeam.add(currentGameInfo.getParticipants().get(i));
      } else {
        redTeam.add(currentGameInfo.getParticipants().get(i));
      }
    }

    ArrayList<Long> listIdPlayers = new ArrayList<>();

    for(int i = 0; i < players.size(); i++) {
      listIdPlayers.add(players.get(i).getSummoner().getId());
    }

    StringBuilder blueTeamString = new StringBuilder();
    StringBuilder blueTeamRankString = new StringBuilder();

    for(int i = 0; i < blueTeam.size(); i++) {
      Champion champion = null;
      champion = Ressources.getChampionDataById(blueTeam.get(i).getChampionId());

      String rank = null;
      try {
        rank = RiotRequest.getSoloqRank(blueTeam.get(i).getSummonerId());
      } catch (RiotApiException e) {
        logger.error(e.getMessage());
        return null;
      }

      if(listIdPlayers.contains(blueTeam.get(i).getSummonerId())) {
        blueTeamString.append(champion.getName() + " | __" + blueTeam.get(i).getSummonerName() + "__" + "\n");
      }else {
        blueTeamString.append(champion.getName() + " | " + blueTeam.get(i).getSummonerName() + "\n");
      }

      blueTeamRankString.append(rank);
      blueTeamRankString.append(rank + "\n");
    }

    message.addField("Équipe Bleu", blueTeamString.toString(), true);
    message.addField("Grades", blueTeamRankString.toString(), true);

    StringBuilder redTeamString = new StringBuilder();
    StringBuilder redTeamRankString = new StringBuilder();

    for(int i = 0; i < redTeam.size(); i++) {
      Champion champion = null;
      champion = Ressources.getChampionDataById(redTeam.get(i).getChampionId());

      String rank = null;
      try {
        rank = RiotRequest.getSoloqRank(redTeam.get(i).getSummonerId());
      } catch (RiotApiException e) {
        logger.error(e.getMessage());
        return null;
      }

      if(listIdPlayers.contains(redTeam.get(i).getSummonerId())) {
        redTeamString.append(champion.getName() + " | __" + redTeam.get(i).getSummonerName() + "__" + "\n");
      }else {
        redTeamString.append(champion.getName() + " | " + redTeam.get(i).getSummonerName() + "\n");
      }

      redTeamRankString.append(rank + "\n");

    }

    message.addBlankField(false);
    message.addField("Équipe Rouge", redTeamString.toString(), true);
    message.addField("Grades", redTeamRankString.toString(), true);

    message.setColor(Color.GREEN);

    return message.build();
  }

  public static MessageEmbed createShowPostulation(Postulation postulation, int postulationNbr) throws RiotApiException {

    EmbedBuilder message = new EmbedBuilder();

    message.setAuthor(postulation.getMember().getUser().getName(), null, postulation.getMember().getUser().getAvatarUrl());

    message.setTitle("Postulation numéro " + postulationNbr + " de " + postulation.getMember().getUser().getName());

    String rank = RiotRequest.getSoloqRank(postulation.getSummoner().getId());
    Field field = new Field("**Pseudo & Rang Soloq**", postulation.getSummoner().getName() + " - " + rank, true);
    message.addField(field);

    String role = "";

    for(int i = 0; i < postulation.getRoles().size(); i++) {
      role += postulation.getRoles().get(i).getName();
      if((i + 1) != postulation.getRoles().size()) {
        role += ", ";
      }
    }

    field = new Field("**Postes**", role, true);
    message.addField(field);

    field = new Field("**Horaires**", postulation.getHoraires(), true);
    message.addField(field);

    message.setColor(Color.GREEN);

    return message.build();
  }
}
