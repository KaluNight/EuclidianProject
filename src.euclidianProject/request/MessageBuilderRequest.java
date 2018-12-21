package request;

import java.awt.Color;

import model.Postulation;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;

public class MessageBuilderRequest {

  public static MessageEmbed createShowPostulation(Postulation postulation, int postulationNbr) {

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
