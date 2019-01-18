package ch.euclidian.main;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.euclidian.main.model.Team;
import ch.euclidian.main.refresh.event.ContinuousTimeChecking;
import ch.euclidian.main.refresh.event.TwitchChannelEvent;
import ch.euclidian.main.util.LogHelper;
import ch.euclidian.main.util.Ressources;
import me.philippheuer.twitch4j.TwitchClient;
import me.philippheuer.twitch4j.TwitchClientBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class EventListener extends ListenerAdapter{

  private static final char PREFIX = '>';

  private static final String ADMIN_ROLE_ID = "497679745551695872";

  private static final String ENREGISTRED_PLAYER_ROLE_NAME = "Enregistré";

  private static final String POSTULANT_PLAYER_ROLE_NAME = "Postulant";

  private static final String ID_LOG_BOT_CHANNEL = "506541176200101909";

  private static final String ID_POSTULATION_CHANNEL = "497763778268495882";

  private static final String ID_REPORT_CHANNEL = "513422522637877266";

  private static Message statusReportMessage;

  private static Timer timerTask;

  private static Logger logger = LoggerFactory.getLogger(EventListener.class);

  private void initializeGuild() {
    Main.setGuild(Main.getLogBot().getGuild());
    Main.setController(Main.getGuild().getController());

    ArrayList<Permission> teamMemberPermissionList = new ArrayList<>();

    //Text Permission
    teamMemberPermissionList.add(Permission.MESSAGE_WRITE);
    teamMemberPermissionList.add(Permission.MESSAGE_READ);
    teamMemberPermissionList.add(Permission.MESSAGE_EMBED_LINKS);
    teamMemberPermissionList.add(Permission.MESSAGE_ATTACH_FILES);
    teamMemberPermissionList.add(Permission.MESSAGE_HISTORY);
    teamMemberPermissionList.add(Permission.MESSAGE_EXT_EMOJI);
    teamMemberPermissionList.add(Permission.MESSAGE_ADD_REACTION);

    //Voice permission
    teamMemberPermissionList.add(Permission.VOICE_CONNECT);
    teamMemberPermissionList.add(Permission.VOICE_USE_VAD);
    teamMemberPermissionList.add(Permission.VOICE_SPEAK);

    Team.setPermissionsList(teamMemberPermissionList);

    if(Main.getGuild().getRolesByName("Enregistré", true).isEmpty()) {
      try {
        RoleAction role = Main.getController().createRole();
        role.setName("Enregistré");
        role.setColor(Color.BLUE);
        role.setMentionable(false);
        role.setPermissions(Team.getPermissionsList());

        Role usableRole = role.complete();
        Main.setRegisteredRole(usableRole);
      } catch (Exception e) {
        LogHelper.logSenderDirectly("Unknow Error : " + e.getMessage());
        logger.error(e.getMessage());
      }
    } else {
      Main.setRegisteredRole(Main.getGuild().getRolesByName("Enregistré", true).get(0));
    }

    if(Main.getGuild().getRolesByName(POSTULANT_PLAYER_ROLE_NAME, true).isEmpty()) {
      LogHelper.logSenderDirectly("Creation d'un rôle postulant ...");
      try {
        RoleAction role = Main.getController().createRole();
        role.setName(POSTULANT_PLAYER_ROLE_NAME);
        role.setColor(Color.YELLOW);
        role.setMentionable(false);
        role.setPermissions(Team.getPermissionsList());

        role.complete();
      } catch (Exception e) {
        LogHelper.logSenderDirectly("Unknow Error : " + e.getMessage());
        logger.error(e.getMessage());
      }
    }else {
      Main.setPostulantRole(Main.getGuild().getRolesByName(POSTULANT_PLAYER_ROLE_NAME, true).get(0));
    }

    ArrayList<Role> posteRole = new ArrayList<>();

    posteRole.add(Main.getGuild().getRolesByName("top", true).get(0));
    posteRole.add(Main.getGuild().getRolesByName("jungle", true).get(0));
    posteRole.add(Main.getGuild().getRolesByName("mid", true).get(0));
    posteRole.add(Main.getGuild().getRolesByName("adc", true).get(0));
    posteRole.add(Main.getGuild().getRolesByName("support", true).get(0));

    Main.setRolePosition(posteRole);

    TextChannel textChannel = Main.getGuild().getTextChannelById(ID_REPORT_CHANNEL);
    List<Message> list = textChannel.getIterableHistory().complete();
    Message message = null;

    for(int i = 0; i < list.size(); i++) {
      if(list.get(i).getAuthor().equals(Main.getJda().getSelfUser())) {
        message = list.get(i);
      }
    }

    if(message == null) {
      message = textChannel.sendMessage("Status : En Ligne").complete();
    } else {
      message.editMessage("Status : En Ligne").complete();
    }

    setStatusReportMessage(message);
  }

  private void setupContinousRefreshThread() {
    ContinuousTimeChecking.setNextTimePanelRefresh(DateTime.now());
    ContinuousTimeChecking.setNextTimeStatusRefresh(DateTime.now());
    ContinuousTimeChecking.setNextTimeSaveData(DateTime.now().plusMinutes(10));
    ContinuousTimeChecking.setNextTimeCheckLive(DateTime.now());

    setTimerTask(new Timer());

    TimerTask mainThread = new ContinuousTimeChecking();
    timerTask.schedule(mainThread, 0, 10000); //10 secondes
  }
  
  private void initTwitchClient() {
    TwitchClient twitchClient = TwitchClientBuilder.init()
        .withClientId(Ressources.getTwitchClientId())
        .withClientSecret(Ressources.getTwitchClientSecret())
        .withCredential(Ressources.getTwitchCredential())
        .withAutoSaveConfiguration(true)
        .build();
    twitchClient.connect();
    
    twitchClient.getDispatcher().registerListener(new TwitchChannelEvent());

    Ressources.setTwitchApi(twitchClient);
    Ressources.setMessageInterface(twitchClient.getMessageInterface());
    Ressources.setChannelEndpoint(twitchClient.getChannelEndpoint());
    Ressources.setStreamEndpoint(twitchClient.getStreamEndpoint());
    
    Ressources.getMessageInterface().joinChannel(Ressources.TWITCH_CHANNEL_NAME);
  }

  @Override
  public void onReady(ReadyEvent event) {
    Main.setLogBot(Main.getJda().getTextChannelById(ID_LOG_BOT_CHANNEL));

    LogHelper.logSenderDirectly("Démarrage...");

    initializeGuild();

    LogHelper.logSenderDirectly("Chargement des champions ...");
    if(Main.loadChampions()) {
      LogHelper.logSenderDirectly("Chargement des champions terminé !");
    }else {
      LogHelper.logSenderDirectly("Une erreur est survenu lors du chargement des champions, les infos cards ne s'afficheront pas !");
    }

    LogHelper.logSenderDirectly("Chargement des sauvegardes détaillés...");

    try {
      Main.loadDataTxt();
    } catch (IOException e) {
      logger.error(e.getMessage());
      LogHelper.logSenderDirectly("Une erreur est survenu lors du chargement des sauvegardes détaillés !");
    } catch (RiotApiException e) {
      logger.error(e.getMessage());
      LogHelper.logSenderDirectly("Une erreur est survenu lors du chargement des sauvegardes détaillés !");
    }

    LogHelper.logSenderDirectly("Chargement des sauvegardes détaillés terminé !");
    LogHelper.logSenderDirectly("Chargement des données des joueurs...");

    try {
      Main.loadPlayerDataWeek();
    }catch (IOException e) {
      logger.error(e.getMessage());
      LogHelper.logSenderDirectly("Une erreur est survenu lors du chagements des données joueurs !");
    }

    LogHelper.logSenderDirectly("Chargement des données des joueurs terminé !");
    LogHelper.logSenderDirectly("Connection à l'api Twitch...");
    
    initTwitchClient();
    
    LogHelper.logSenderDirectly("Connection à l'api Twitch effectué !");
    LogHelper.logSenderDirectly("Démarrage des tâches continue...");

    setupContinousRefreshThread();

    LogHelper.logSenderDirectly("Démarrage des tâches continues terminés !");

    LogHelper.logSenderDirectly("Démarrage terminés !");
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    String message = event.getMessage().getContentRaw();

    List<Role> rolesOfSender = null;

    try {
      rolesOfSender = initializeRolesFromSender(event);
    }catch (NullPointerException e) {
      logger.info("L'envoyeur ne fait plus parti du serveur");
      return;
    }

    boolean isAdmin = false;

    isAdmin = isAdminByRoles(rolesOfSender);

    if(event.getTextChannel().getId().equals(ID_POSTULATION_CHANNEL) && message.charAt(0) != PREFIX && !isAdmin
        && !Main.getJda().getSelfUser().equals(event.getAuthor())){
      event.getMessage().delete().queue();

      PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
      privateChannel.sendTyping().queue();
      privateChannel.sendMessage("On envoie uniquement des demandes de Postulation sur ce channel ! "
          + "(Note : Une postulation commence par \">postulation\")").queue();

    }

    if(event.getTextChannel().getId().equals(ID_REPORT_CHANNEL) && !Main.getJda().getSelfUser().equals(event.getAuthor())) {

      event.getMessage().delete().complete();

      message = "Message de " + event.getAuthor().getName() + " :\n" + message;
      Main.addReport(message);

      PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
      privateChannel.sendMessage("Ton message a été envoyé, nous te répondrons dans les plus brefs délais !").queue();
    }

    if (message.length() == 0 || message.charAt(0) != PREFIX) {
      return;
    }

    message = message.substring(1);

    if(event.getTextChannel().getName().equals("postulation")) {
      String[] postulation = message.split("\n");
      if(postulation[0].equals("Postulation")) {
        event.getTextChannel().sendTyping().complete();
        String result = CommandManagement.postulationCommand(postulation, event.getMember());
        Message messageSend = event.getTextChannel().sendMessage(result).complete();

        PrivateChannel pc = event.getAuthor().openPrivateChannel().complete();

        pc.sendMessage("~Copie du Message~\n" + result).complete();

        event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
        messageSend.delete().queueAfter(10, TimeUnit.SECONDS);
      }else {
        if(!isAdmin) {
          event.getTextChannel().sendTyping().complete();
          Message messageResponse = event.getTextChannel().sendMessage("Les demandes de Postulation doivent commencer par \"Postulation\".").complete();
          event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
          messageResponse.delete().queueAfter(5, TimeUnit.SECONDS);
        }
      }
    }

    String command = message.split(" ")[0];

    if(isAdmin) {

      if (command.equalsIgnoreCase("Add")){
        event.getTextChannel().sendTyping().complete();
        String result = CommandManagement.addCommand(message.substring(4), event.getAuthor());
        event.getTextChannel().sendMessage(result).queue();

      } else if (command.equalsIgnoreCase("show")) {

        event.getTextChannel().sendTyping().complete();

        if(message.split(" ")[1].equalsIgnoreCase("postulations") || message.split(" ")[1].equalsIgnoreCase("postulation")) {

          ArrayList<MessageEmbed> listEmbended = null;
          try {
            listEmbended = new ArrayList<>(CommandManagement.showPostulationsCommand(command));
          } catch (RiotApiException e) {
            e.printStackTrace();
            event.getTextChannel().sendMessage("Erreur api").queue();
            return;
          }

          for(int i = 0; i < listEmbended.size(); i++) {
            event.getTextChannel().sendMessage(listEmbended.get(i)).queue();
          }

          if(listEmbended.isEmpty()) {
            event.getTextChannel().sendMessage("Aucune Postulation dans la liste").queue();
          }

        } else if(message.split(" ")[1].equalsIgnoreCase("reports") || message.split(" ")[1].equalsIgnoreCase("report")) {
          event.getChannel().sendTyping().complete();
          ArrayList<String> listReport = new ArrayList<>(CommandManagement.showReportsCommand());
          for(int i = 0; i < listReport.size(); i++) {
            event.getChannel().sendMessage(listReport.get(i)).queue();
          }

          if(listReport.isEmpty()) {
            event.getChannel().sendMessage("Aucun message a afficher").queue();
          }

        } else {
          String result = CommandManagement.showCommand(command, event.getAuthor());
          event.getTextChannel().sendMessage(result).queue();
        }

      } else if(command.equalsIgnoreCase("register") && message.contains("new")) {

        List<Member> members = event.getMessage().getMentionedMembers();

        if(members.size() == 1) {
          event.getTextChannel().sendTyping().queue();
          String result = CommandManagement.registerCommand(message.substring(9), members.get(0).getUser(), false);
          event.getTextChannel().sendMessage(result).queue();

        }else {
          event.getTextChannel().sendMessage("Merci d'envoyer la commande dans ce format pour enregistrer quelqu'un : "
              + "\">register new PseudoLol MentionDuJoueur\"").queue();
        }
        return;

      } else if (command.equalsIgnoreCase("postulation")){

        event.getTextChannel().sendTyping().complete();
        String result = CommandManagement.postulationCommand(message.substring(12), event.getAuthor());
        event.getTextChannel().sendMessage(result).queue();

      } else if (command.equalsIgnoreCase("delete")) {

        event.getTextChannel().sendTyping().complete();
        String result = CommandManagement.deleteCommand(message.substring(7));
        event.getTextChannel().sendMessage(result).queue();


      }else if(command.equalsIgnoreCase("clear")) {

        event.getTextChannel().sendTyping().complete();
        String result = CommandManagement.clearCommand(message.substring(6));
        event.getTextChannel().sendMessage(result).queue();

      } else if(command.equalsIgnoreCase("getAccountId")) {

        event.getTextChannel().sendTyping().complete();
        Summoner result;
        try {
          result = Ressources.getRiotApi().getSummonerByName(Platform.EUW, message.substring(13));
        } catch (RiotApiException e) {
          e.printStackTrace();
          event.getTextChannel().sendMessage("Erreur d'app").queue();
          return;
        }
        event.getTextChannel().sendMessage("Account Id de " + result.getName() + " : " + result.getAccountId()).queue();

      } else if (command.equals("stop")) {
        statusReportMessage.editMessage("Status : Hors Ligne").complete();
        event.getTextChannel().sendTyping().complete();
        
        timerTask.cancel();
        ContinuousTimeChecking.shutdownThreadPool();
        event.getTextChannel().sendMessage("Je suis down !").complete();
        Main.getJda().shutdownNow();
        
        try {
          Main.saveDataTxt();
        } catch (IOException e) {
          logger.error("Erreur de sauvegarde : {}", e.getMessage());
        }
        
        try {
          Ressources.getMessageInterface().leaveChannel(Ressources.TWITCH_CHANNEL_NAME);
        } catch (NullPointerException e) {
          logger.warn("NullPointerException : {}", e.getMessage());
        }
        
        Ressources.getTwitchApi().disconnect();
        System.exit(0);
      }
    }

    if (command.equalsIgnoreCase("register")) {
      event.getTextChannel().sendTyping().queue();
      String result = CommandManagement.registerCommand(message.substring(9), event.getAuthor(), true);
      event.getTextChannel().sendMessage(result).queue();
    }
  }

  private List<Role> initializeRolesFromSender(MessageReceivedEvent event) {
    List<Role> roles = new ArrayList<>();
    roles.addAll(event.getMember().getRoles());   
    return roles;
  }

  private boolean isAdminByRoles(List<Role> rolesOfSender) {
    boolean isAdmin = false;
    for(int i = 0; i < rolesOfSender.size(); i++) {
      if(rolesOfSender.get(i).getId().equals(ADMIN_ROLE_ID)) {
        isAdmin = true;
      }
    }
    return isAdmin;
  }

  public static String getEnregistredPlayerRoleName() {
    return ENREGISTRED_PLAYER_ROLE_NAME;
  }

  public static Timer getTimerTask() {
    return timerTask;
  }

  public static void setTimerTask(Timer timerTask) {
    EventListener.timerTask = timerTask;
  }

  public static void setStatusReportMessage(Message statusReportMessage) {
    EventListener.statusReportMessage = statusReportMessage;
  }
}
