package request;

import java.awt.Color;
import java.util.ArrayList;

import org.checkerframework.checker.units.qual.m;

import model.Postulation;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.User;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameParticipant;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;
import util.Ressources;

public class MessageBuilderRequest {
  
  private MessageBuilderRequest() {
  }
  
  public static MessageEmbed createInfoCard1summoner(User user, Summoner summoner, CurrentGameInfo match) {
    
    EmbedBuilder message = new EmbedBuilder();
    
    message.setAuthor(user.getName(), null, user.getAvatarUrl());
    
    message.setTitle("Info sur la partie de " + user.getName());
    
    int blueTeamID = 0;
    int redTeamID = 0;
    
    for(int i = 0; i < match.getParticipants().size(); i++) {
      if(i == 0) {
        blueTeamID = match.getParticipants().get(i).getTeamId();
      }
      
      if(blueTeamID != match.getParticipants().get(i).getTeamId()) {
        redTeamID = match.getParticipants().get(i).getTeamId();
        break;
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
    
    for(int i = 0; i < blueTeam.size(); i++) {
    	String rank;
    	
    	blueTeamString.append(blueTeam.get(i).getSummonerName() + " | " + rank);
    }
    
    Field field = new Field("Équipe Bleu", value, inline);
    
    
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
