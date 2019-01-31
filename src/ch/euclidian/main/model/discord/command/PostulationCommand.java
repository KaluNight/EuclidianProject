package ch.euclidian.main.model.discord.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import ch.euclidian.main.Main;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.model.Postulation;
import ch.euclidian.main.util.LogHelper;
import ch.euclidian.main.util.Ressources;
import ch.euclidian.main.util.request.MessageBuilderRequest;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class PostulationCommand extends Command {

  private static final int NUMBER_BEFORE_MESSAGE_DELETION = 10;
  private static final TimeUnit TIME_UNIT_TYPE = TimeUnit.SECONDS;

  private final EventWaiter waiter;
  private Summoner summoner;
  private static final HashMap<User, List<Role>> listRole = new HashMap<>();
  private static final HashMap<User, List<Message>> messages = new HashMap<>();
  private static final HashMap<User, Postulation> postulations = new HashMap<>();
  private static final List<User> userInRegistration = new ArrayList<>();

  public PostulationCommand(EventWaiter waiter) {
    this.waiter = waiter;
    this.name = "postulation";
    this.aliases = new String[] {"post"};
    this.help = "Lance une procédure de postulation";
    this.guildOnly = true;
  }

  @Override
  protected void execute(CommandEvent event) {
    if(messages.get(event.getAuthor()) == null) {

      messages.put(event.getAuthor(), new ArrayList<>());

      if(Main.getPostulationIndexByMember(event.getMember()) != -1) {
        addMessageToList(event.getAuthor(),
            event.getTextChannel()
                .sendMessage("Vous avez déjà créer votre postulation !" + " Vous pourrez signaler des modifications lors de l'entretien")
                .complete());
        addMessageToList(event.getAuthor(), event.getMessage());

        endRegistrationWithoutUserDelete(event.getEvent());
        return;
      }

      addMessageToList(event.getAuthor(), event.getMessage());

      Player player = Main.getPlayersByDiscordId(event.getAuthor().getId());

      if(player != null) {
        addMessageToList(event.getAuthor(), event.getEvent().getTextChannel().sendMessage("Vous êtes déjà enregistré !").complete());
        endRegistrationWithoutUserDelete(event.getEvent());
        return;
      }

      if(userInRegistration.contains(event.getAuthor())) {
        return;
      } else {
        userInRegistration.add(event.getAuthor());
      }

      addMessageToList(event.getAuthor(),
          event.getTextChannel().sendMessage("Bien ! Sachez tous d'abord que certaine réponses peuvent peut-être prendre du temps"
              + " avant d'être envoyé en fonction de la disponiblité des serveurs de Riot, si cela fait plus de 5 minutes que je n'ai pas"
              + " donné de réponses c'est que j'ai subis une erreur interne. Dans ce cas merci de contacter un admin."
              + "\nCommençons ! Donnez moi d'abord votre **pseudo LoL** __**Parfaitement écrit**__").complete());

      waiter.waitForEvent(MessageReceivedEvent.class,
          e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()), e -> receivePseudoAndContinue(e), 1,
          TimeUnit.MINUTES, () -> endRegistrationTime(event.getEvent()));
    }
  }

  private void receivePseudoAndContinue(MessageReceivedEvent messageEvent) {
    messageEvent.getTextChannel().sendTyping().complete();
    String pseudo = messageEvent.getMessage().getContentRaw();

    addMessageToList(messageEvent.getAuthor(), messageEvent.getMessage());

    try {
      summoner = Ressources.getRiotApi().getSummonerByName(Platform.EUW, pseudo);
    } catch(IllegalArgumentException e) {
      addMessageToList(messageEvent.getAuthor(),
          messageEvent.getTextChannel()
              .sendMessage("Votre pseudo n'est pas valide." + " Merci de vérifier la typographie du pseudo et de renvoyer le pseudo correct"
                  + " (Note : Il doit obligatoirement être de la région EUW)")
              .complete());

      waiter.waitForEvent(MessageReceivedEvent.class,
          e1 -> e1.getAuthor().equals(messageEvent.getAuthor()) && e1.getChannel().equals(messageEvent.getChannel()),
          e1 -> receivePseudoAndContinue(e1), 1, TimeUnit.MINUTES, () -> endRegistrationTime(messageEvent));

    } catch(RiotApiException e) {
      if(e.getErrorCode() == 404) {
        addMessageToList(messageEvent.getAuthor(),
            messageEvent.getTextChannel()
                .sendMessage(
                    "Votre pseudo n'a pas été trouvé." + " Merci de vérifier la typographie du pseudo et de renvoyer le pseudo correct"
                        + " (Note : Il doit obligatoirement être de la région EUW)")
                .complete());

        waiter.waitForEvent(MessageReceivedEvent.class,
            e1 -> e1.getAuthor().equals(messageEvent.getAuthor()) && e1.getChannel().equals(messageEvent.getChannel()),
            e1 -> receivePseudoAndContinue(e1), 1, TimeUnit.MINUTES, () -> endRegistrationTime(messageEvent));
      } else {
        Message message = messageEvent.getTextChannel().sendMessage("Les serveurs de riot ont actuellement des problèmes et ne peuvent pas"
            + " donc pas valider votre pseudo. Merci de réessayer plus tard.").complete();

        message.delete().queueAfter(NUMBER_BEFORE_MESSAGE_DELETION, TIME_UNIT_TYPE);

        endRegistration(messageEvent);
        return;
      }
    }

    for(Player player : Main.getPlayerList()) {
      if(player.getSummoner().getId() == summoner.getId()) {
        Message message = messageEvent.getTextChannel()
            .sendMessage("Ce compte est déjà enregistré,"
                + " si c'est le votre et que quelqu'un c'est enregistré a votre place, veuillez contacter un @KaluNight#0001."
                + " La procédure de postulation à été stoppée")
            .complete();
        message.delete().queueAfter(NUMBER_BEFORE_MESSAGE_DELETION, TIME_UNIT_TYPE);
        endRegistration(messageEvent);
        return;
      }
    }

    if(summoner != null) {
      addMessageToList(messageEvent.getAuthor(),
          messageEvent.getTextChannel()
              .sendMessage("Très bien, dites moi maintenant quel rôle vous souhaitez jouer (1 seul) en répondant **exactement** soit :"
                  + " **Mid**, **Support**, **Jungle**, **Top**, **ADC**")
              .complete());

      waiter.waitForEvent(MessageReceivedEvent.class,
          e -> e.getAuthor().equals(messageEvent.getAuthor()) && e.getChannel().equals(messageEvent.getChannel()),
          e -> receiveRoleAndContinue(e), 1, TimeUnit.MINUTES, () -> endRegistrationTime(messageEvent));
    }
  }

  private void receiveRoleAndContinue(MessageReceivedEvent messageEvent) {
    listRole.put(messageEvent.getAuthor(), new ArrayList<>());
    askForAllRole(messageEvent);
  }

  private void askForAllRole(MessageReceivedEvent event) {
    event.getTextChannel().sendTyping();
    String roleStr = event.getMessage().getContentRaw();

    addMessageToList(event.getAuthor(), event.getMessage());

    if(roleStr.equalsIgnoreCase("Ok")) {
      if(listRole.get(event.getAuthor()).isEmpty()) {
        addMessageToList(event.getAuthor(),
            event.getTextChannel()
                .sendMessage("Vous n'avez saisi aucun poste, postulation annulé." + " Vous pouvez la recommencer quand vous le souhaitez")
                .complete());

        endRegistration(event);
      } else {
        addMessageToList(event.getAuthor(), event.getTextChannel().sendMessage("Plus qu'une étape ! Noter maintenant vos disponibilités."
            + " (Ex : Vendredi soir, Samedi après midi, Dimanche soir et après midi)").complete());

        waiter.waitForEvent(MessageReceivedEvent.class,
            e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()), e -> timeRegistration(e), 1,
            TimeUnit.MINUTES, () -> endRegistrationTime(event));
      }
    } else {
      Role role = Main.getPositionRoleByName(roleStr);
      if(role != null) {
        if(!containForUser(event.getAuthor(), role)) {

          addRoleToList(event.getAuthor(), role);

          addMessageToList(event.getAuthor(),
              event.getTextChannel().sendMessage("Bien, avez vous un **autre rôle** auquel vous souhaitez postuler ?"
                  + " Si c'est le cas vous pouvez le noter, sinon vous pouvez envoyer **Ok**").complete());

          waiter.waitForEvent(MessageReceivedEvent.class,
              e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()), e -> askForAllRole(e), 1,
              TimeUnit.MINUTES, () -> endRegistrationTime(event));
        } else {
          addMessageToList(event.getAuthor(), event.getTextChannel().sendMessage("Le rôle que vous avez écrit a déjà été inscrit."
              + " Si vous souhaitez ajouter un **autre poste** écrivez le, sinon écriver **Ok**").complete());

          waiter.waitForEvent(MessageReceivedEvent.class,
              e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()), e -> askForAllRole(e), 1,
              TimeUnit.MINUTES, () -> endRegistrationTime(event));
        }
      } else {
        addMessageToList(event.getAuthor(),
            event.getTextChannel().sendMessage("Le rôle que vous avez écrit est invalide, merci de vérifier l'orthographe."
                + " (**Top**, **Jungle**, **Mid**, **ADC**, **Support** ou **Ok** si vous avez écris tous vos postes)").complete());

        waiter.waitForEvent(MessageReceivedEvent.class,
            e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()), e -> askForAllRole(e), 1,
            TimeUnit.MINUTES, () -> endRegistrationTime(event));
      }
    }
  }

  private void timeRegistration(MessageReceivedEvent event) {
    event.getTextChannel().sendTyping().complete();
    String dispo = event.getMessage().getContentRaw();

    addMessageToList(event.getAuthor(), event.getMessage());

    MessageBuilder builder = new MessageBuilder();

    builder.append("Terminé ! Valider vous les informations ci-dessous ? Répondez **Ok** pour valider l'enregistrement,"
        + " sinon **Stop** pour l'annuler");

    postulations.put(event.getAuthor(), new Postulation(event.getMember(), summoner, listRole.get(event.getAuthor()), dispo));

    MessageEmbed embended;
    embended = MessageBuilderRequest.createShowPostulation(postulations.get(event.getAuthor()), 1);

    Message validationMessage = builder.build();

    addMessageToList(event.getAuthor(), event.getTextChannel().sendMessage(validationMessage).complete());
    addMessageToList(event.getAuthor(), event.getTextChannel().sendMessage(embended).complete());

    waiter.waitForEvent(MessageReceivedEvent.class,
        e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()), e -> validation(e), 1, TimeUnit.MINUTES,
        () -> endRegistrationTime(event));
  }

  private void validation(MessageReceivedEvent event) {
    event.getTextChannel().sendTyping().complete();
    String response = event.getMessage().getContentRaw();
    addMessageToList(event.getAuthor(), event.getMessage());

    if(response.equalsIgnoreCase("Ok")) {
      Main.getPostulationsList().add(postulations.get(event.getAuthor()));
      Main.getController().addRolesToMember(event.getMember(), listRole.get(event.getAuthor())).queue();
      Main.getController().addSingleRoleToMember(event.getMember(), Main.getPostulantRole()).queue();

      LogHelper.logSender("Nouvelle postulation créé par " + event.getAuthor().getName());

      addMessageToList(event.getAuthor(),
          event.getTextChannel()
              .sendMessage("Merci d'avoir postulé ! Vous recevrez des informations concernant votre potentiel recrutement très bientôt !")
              .complete());

      try {
        PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
        privateChannel.sendMessage("Merci d'avoir postulé ! On essaye en général de répondre dans les 72h"
            + " mais il arrive parfois qu'on y arrive pas à temps. N'hésitez pas à nous le signaler (aux admins) si cela devait arriver,"
            + " on s'occupera au plus vite de vous.\nBonne Journée !").complete();
      } finally {
        endRegistration(event);
      }

    } else if(response.equalsIgnoreCase("Stop")) {
      addMessageToList(event.getAuthor(), event.getTextChannel()
          .sendMessage("Vous avez décidé d'annuler votre postulation. Vous pouvez à tous moment en refaire une").complete());
      endRegistration(event);
    } else {
      addMessageToList(event.getAuthor(),
          event.getTextChannel()
              .sendMessage(
                  "Le message que vous avez envoyé ne correspond pas à Ok ou Stop. Merci de écrire exactement l'une de ces réponsese")
              .complete());

      waiter.waitForEvent(MessageReceivedEvent.class,
          e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()), e -> validation(e), 1,
          TimeUnit.MINUTES, () -> endRegistrationTime(event));
    }
  }

  private void endRegistrationTime(MessageReceivedEvent event) {
    Message messageToDelete =
        event.getTextChannel().sendMessage("Vous avez pris trop de temps pour répondre, merci de recommencer la procédure").complete();

    messageToDelete.delete().queueAfter(NUMBER_BEFORE_MESSAGE_DELETION, TIME_UNIT_TYPE);

    endRegistration(event);
  }

  private void endRegistration(MessageReceivedEvent event) {
    TextChannel textChannel = event.getTextChannel();

    textChannel.deleteMessages(messages.get(event.getAuthor())).queueAfter(NUMBER_BEFORE_MESSAGE_DELETION, TIME_UNIT_TYPE);

    userInRegistration.remove(event.getAuthor());

    messages.remove(event.getAuthor());
    listRole.remove(event.getAuthor());
    postulations.remove(event.getAuthor());
  }


  private void endRegistrationWithoutUserDelete(MessageReceivedEvent event) {

    TextChannel textChannel = event.getTextChannel();

    textChannel.deleteMessages(messages.get(event.getAuthor())).queueAfter(NUMBER_BEFORE_MESSAGE_DELETION, TIME_UNIT_TYPE);

    messages.remove(event.getAuthor());
    listRole.remove(event.getAuthor());
    postulations.remove(event.getAuthor());
  }

  private void addMessageToList(User user, Message message) {
    List<Message> messageOfUser = messages.get(user);
    messageOfUser.add(message);
  }

  private void addRoleToList(User user, Role role) {
    List<Role> roleOfUser = listRole.get(user);
    roleOfUser.add(role);
  }

  private boolean containForUser(User user, Role role) {
    List<Role> roleOfUser = listRole.get(user);
    return roleOfUser.contains(role);
  }

  public static List<User> getUserInRegistration() {
    return userInRegistration;
  }
}
