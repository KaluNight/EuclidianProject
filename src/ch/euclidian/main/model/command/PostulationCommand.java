package ch.euclidian.main.model.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import ch.euclidian.main.Main;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.util.Ressources;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class PostulationCommand extends Command{

  private static final int NUMBER_BEFORE_MESSAGE_DELETION = 10;
  private static final TimeUnit TIME_UNIT_TYPE = TimeUnit.SECONDS;

  private final EventWaiter waiter;
  private Summoner summoner;
  private List<Role> listRole;
  private List<Message> messages;

  public PostulationCommand(EventWaiter waiter) {
    this.waiter = waiter;
    this.name = "postulation";
    this.aliases = new String[]{"post"};
    this.help = "Lance une procédure de postulation";
  }

  @Override
  protected void execute(CommandEvent event) {
    messages = new ArrayList<>();

    messages.add(event.getMessage());

    messages.add(event.getTextChannel().sendMessage("Bien ! Tous d'abord, donnez moi votre pseudo LoL").complete());

    waiter.waitForEvent(MessageReceivedEvent.class,
        e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()),
        e -> receivePseudoAndContinue(e),
        1, TimeUnit.MINUTES, () -> endRegistrationTime(event.getEvent()));
  }

  private void receivePseudoAndContinue(MessageReceivedEvent messageEvent){
    String pseudo = messageEvent.getMessage().getContentRaw();

    messages.add(messageEvent.getMessage());

    try {
      summoner = Ressources.getRiotApi().getSummonerByName(Platform.EUW, pseudo);
    } catch (IllegalArgumentException e) {
      Message message = messageEvent.getTextChannel().sendMessage("Votre pseudo n'est pas valide."
          + " Merci de vérifier la typographie du pseudo et de recommencer la procédure une fois cela fait"
          + " (Note : Il doit obligatoirement être de la région EUW)").complete();

      message.delete().queueAfter(NUMBER_BEFORE_MESSAGE_DELETION, TIME_UNIT_TYPE);

      endRegistration();
      return;
    } catch (RiotApiException e) {
      Message message = messageEvent.getTextChannel().sendMessage("L'api à rencontré un problème. Merci de réessayer plus tard")
          .complete();

      message.delete().queueAfter(NUMBER_BEFORE_MESSAGE_DELETION, TIME_UNIT_TYPE);

      endRegistration();
      return;
    }

    for(Player player : Main.getPlayerList()) {
      if(player.getSummoner().getId() == summoner.getId()) {
        Message message = messageEvent.getTextChannel()
            .sendMessage("Ce compte est déjà enregistré,"
                + " si c'est le votre et que quelqu'un c'est enregistré a votre place, veuillez contacter un @KaluNight#0001."
                + " La procédure de postulation à été stoppée").complete();
        message.delete().queueAfter(NUMBER_BEFORE_MESSAGE_DELETION, TIME_UNIT_TYPE);
        endRegistration();
        return;
      }
    }

    messages.add(messageEvent.getTextChannel()
        .sendMessage("Très bien, dites nous maintenant quel rôle vous souhaitez jouer (1 seul) en répondant **exactement** soit :"
            + " Mid, Support, Jungle, Top, ADC").complete());

    waiter.waitForEvent(MessageReceivedEvent.class,
        e -> e.getAuthor().equals(messageEvent.getAuthor()) && e.getChannel().equals(messageEvent.getChannel()),
        e -> receiveRoleAndContinue(e),
        1, TimeUnit.MINUTES, () -> endRegistrationTime(messageEvent));
  }

  private void receiveRoleAndContinue(MessageReceivedEvent messageEvent) {
    listRole = new ArrayList<>();

    askForAllRole(messageEvent);
  }

  private void askForAllRole(MessageReceivedEvent event) {
    String roleStr = event.getMessage().getContentRaw();

    messages.add(event.getMessage());

    if(roleStr.equalsIgnoreCase("Ok")) {
      if(listRole.isEmpty()) {
        messages.add(event.getTextChannel().sendMessage("Vous n'avez saisi aucun poste, postulation annulé."
            + " Vous pouvez la recommencer quand vous le souhaitez").complete());
        
        endRegistration();
        return;
      }else {
        messages.add(event.getTextChannel().sendMessage("Plus qu'une étape ! Noter maintenant vos disponibilités."
            + " (Ex : Vendredi soir, Samedi après midi, Dimanche soir et après midi)").complete());
        
        waiter.waitForEvent(MessageReceivedEvent.class,
            e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()),
            e -> timeRegistration(e),
            1, TimeUnit.MINUTES, () -> endRegistrationTime(event));
        return;
      }
    }else {
      Role role = Main.getPositionRoleByName(roleStr);
      if(role != null) {
        if(!listRole.contains(role)) {

          listRole.add(role);

          messages.add(event.getTextChannel().sendMessage("Bien, avez vous un autre rôle auquel vous souhaitez postuler ?"
              + " Si c'est le cas vous pouvez le noter, sinon vous pouvez envoyer Ok").complete());
          
          waiter.waitForEvent(MessageReceivedEvent.class,
              e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()),
              e -> askForAllRole(e),
              1, TimeUnit.MINUTES, () -> endRegistrationTime(event));
        } else {
          messages.add(event.getTextChannel().sendMessage("Le rôle que vous avez écrit a déjà été inscrit."
              + " Si vous souhaitez ajouter un autre poste écrivez le, sinon écriver Ok").complete());
          
          waiter.waitForEvent(MessageReceivedEvent.class,
              e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()),
              e -> askForAllRole(e),
              1, TimeUnit.MINUTES, () -> endRegistrationTime(event));
        }
      }else {
        messages.add(event.getTextChannel().sendMessage("Le rôle que vous avez écrit est invalide, merci de vérifier l'orthographe."
            + " (Top, Jungle, Mid, ADC, Support ou Ok si vous avez écris tous vos postes)").complete());

        waiter.waitForEvent(MessageReceivedEvent.class,
            e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()),
            e -> askForAllRole(e),
            1, TimeUnit.MINUTES, () -> endRegistrationTime(event));
      }
    }
  }

  private void timeRegistration(MessageReceivedEvent event) {

  }

  private void endRegistrationTime(MessageReceivedEvent event) {
    Message messageToDelete = event.getTextChannel()
        .sendMessage("Vous avez pris trop de temps pour répondre, merci de recommencer la procédure").complete();

    messageToDelete.delete().queueAfter(NUMBER_BEFORE_MESSAGE_DELETION, TIME_UNIT_TYPE);

    endRegistration();
  }

  private void endRegistration() {
    for(Message message : messages) {
      message.delete().queue();
    }
  }

}
