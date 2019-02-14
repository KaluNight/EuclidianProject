package ch.euclidian.main;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.euclidian.main.model.Champion;
import ch.euclidian.main.model.CustomEmote;
import ch.euclidian.main.model.Team;
import ch.euclidian.main.model.discord.command.PostulationCommand;
import ch.euclidian.main.model.twitch.command.LinkDiscordCommand;
import ch.euclidian.main.model.twitch.command.TopEloCommand;
import ch.euclidian.main.music.BotMusicManager;
import ch.euclidian.main.refresh.event.ContinuousTimeChecking;
import ch.euclidian.main.refresh.event.TwitchChannelEvent;
import ch.euclidian.main.util.LogHelper;
import ch.euclidian.main.util.Ressources;
import me.philippheuer.twitch4j.TwitchClient;
import me.philippheuer.twitch4j.TwitchClientBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class EventListener extends ListenerAdapter {

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

    Map<Double, Object> tablCorrespondanceRank = new TreeMap<>();
    tablCorrespondanceRank.put(1000.0, "Fer 4");
    tablCorrespondanceRank.put(1100.0, "Fer 3");
    tablCorrespondanceRank.put(1200.0, "Fer 2");
    tablCorrespondanceRank.put(1300.0, "Fer 1");
    tablCorrespondanceRank.put(1400.0, "Bronze 4");
    tablCorrespondanceRank.put(1500.0, "Bronze 3");
    tablCorrespondanceRank.put(1600.0, "Bronze 2");
    tablCorrespondanceRank.put(1700.0, "Bronze 1");
    tablCorrespondanceRank.put(1800.0, "Argent 4");
    tablCorrespondanceRank.put(1900.0, "Argent 3");
    tablCorrespondanceRank.put(2000.0, "Argent 2");
    tablCorrespondanceRank.put(2100.0, "Argent 1");
    tablCorrespondanceRank.put(2200.0, "Or 4");
    tablCorrespondanceRank.put(2300.0, "Or 3");
    tablCorrespondanceRank.put(2400.0, "Or 2");
    tablCorrespondanceRank.put(2500.0, "Or 1");
    tablCorrespondanceRank.put(2600.0, "Platine 4");
    tablCorrespondanceRank.put(2700.0, "Platine 3");
    tablCorrespondanceRank.put(2800.0, "Platine 2");
    tablCorrespondanceRank.put(2900.0, "Platine 1");
    tablCorrespondanceRank.put(3000.0, "Diamant 4");
    tablCorrespondanceRank.put(3100.0, "Diamant 3");
    tablCorrespondanceRank.put(3200.0, "Diamant 2");
    tablCorrespondanceRank.put(3300.0, "Diamant 1");
    tablCorrespondanceRank.put(3400.0, "Maître+");

    Ressources.setTableCorrespondanceRank(tablCorrespondanceRank);


    ArrayList<Permission> teamMemberPermissionList = new ArrayList<>();

    // Text Permission
    teamMemberPermissionList.add(Permission.MESSAGE_WRITE);
    teamMemberPermissionList.add(Permission.MESSAGE_READ);
    teamMemberPermissionList.add(Permission.MESSAGE_EMBED_LINKS);
    teamMemberPermissionList.add(Permission.MESSAGE_ATTACH_FILES);
    teamMemberPermissionList.add(Permission.MESSAGE_HISTORY);
    teamMemberPermissionList.add(Permission.MESSAGE_EXT_EMOJI);
    teamMemberPermissionList.add(Permission.MESSAGE_ADD_REACTION);

    // Voice permission
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
      } catch(Exception e) {
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
      } catch(Exception e) {
        LogHelper.logSenderDirectly("Unknow Error : " + e.getMessage());
        logger.error(e.getMessage());
      }
    } else {
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
    timerTask.schedule(mainThread, 0, 10000); // 10 secondes
  }

  private void initTwitchClient() {
    TwitchClient twitchClient =
        TwitchClientBuilder.init().withClientId(Ressources.getTwitchClientId()).withClientSecret(Ressources.getTwitchClientSecret())
        .withCredential(Ressources.getTwitchCredential()).withAutoSaveConfiguration(true).build();
    twitchClient.connect();

    twitchClient.getDispatcher().registerListener(new TwitchChannelEvent());

    twitchClient.getCommandHandler().registerCommand(TopEloCommand.class);
    twitchClient.getCommandHandler().registerCommand(LinkDiscordCommand.class);

    Ressources.setTwitchApi(twitchClient);
    Ressources.setMessageInterface(twitchClient.getMessageInterface());
    Ressources.setChannelEndpoint(twitchClient.getChannelEndpoint());
    Ressources.setStreamEndpoint(twitchClient.getStreamEndpoint());

    Ressources.getMessageInterface().joinChannel(Ressources.TWITCH_CHANNEL_NAME);
  }

  private void initMusicBot() {
    BotMusicManager musicBot = new BotMusicManager();
    Main.getGuild().getAudioManager().setSendingHandler(musicBot.getMusicManager().getSendHandler());
    musicBot.setAudioManager(Main.getGuild().getAudioManager());
    Ressources.setMusicBot(musicBot);
  }

  private void loadCustomEmotes() throws IOException {
    List<Emote> uploadedEmotes = getAllGuildCustomEmotes();
    List<CustomEmote> picturesInFile = Main.loadPicturesInFile();

    assigneAlreadyUploadedEmoteToPicturesInFile(uploadedEmotes, picturesInFile);

    List<CustomEmote> emotesNeedToBeUploaded = getEmoteNeedToBeUploaded(picturesInFile);

    Main.prepareUploadOfEmotes(emotesNeedToBeUploaded);

    List<CustomEmote> emoteAlreadyUploded = getEmoteAlreadyUploaded(picturesInFile);

    Ressources.setCustomEmote(emoteAlreadyUploded);
  }

  private List<CustomEmote> getEmoteAlreadyUploaded(List<CustomEmote> picturesInFile) {
    List<CustomEmote> emoteAlreadyUploded = new ArrayList<>();

    for(CustomEmote customEmote : picturesInFile) {
      if(customEmote.getEmote() != null) {
        emoteAlreadyUploded.add(customEmote);
      }
    }
    return emoteAlreadyUploded;
  }

  private List<CustomEmote> getEmoteNeedToBeUploaded(List<CustomEmote> picturesInFile) {
    List<CustomEmote> emotesNeedToBeUploaded = new ArrayList<>();

    for(CustomEmote customEmote : picturesInFile) {
      if(customEmote.getEmote() == null) {
        emotesNeedToBeUploaded.add(customEmote);
      }
    }
    return emotesNeedToBeUploaded;
  }

  private void assigneAlreadyUploadedEmoteToPicturesInFile(List<Emote> uploadedEmotes, List<CustomEmote> picturesInFile) {
    for(CustomEmote customeEmote : picturesInFile) {
      for(Emote emote : uploadedEmotes) {
        if(emote.getName().equalsIgnoreCase(customeEmote.getName())) {
          customeEmote.setEmote(emote);
        }
      }
    }
  }

  private List<Emote> getAllGuildCustomEmotes() {
    List<Emote> uploadedEmotes = new ArrayList<>();
    List<Guild> listGuild = Main.getJda().getGuilds();

    for(Guild guild : listGuild) {
      uploadedEmotes.addAll(guild.getEmotes());

      if(!guild.getName().equals("Bataillon Euclidien")) {
        guild.delete().complete(); //TODO DELETE WHEN DEBUG FINISH
      }
    }
    return uploadedEmotes;
  }

  private void assigneEmotesToChampion() {

    for(CustomEmote emote : Ressources.getCustomEmote()) {
      for(Champion champion : Ressources.getChampions()) {
        if(champion.getId().equals(emote.getFile().getName())) {
          champion.setEmote(emote.getEmote());
        }
      }
    }
  }

  @Override
  public void onReady(ReadyEvent event) {
    Main.setLogBot(Main.getJda().getTextChannelById(ID_LOG_BOT_CHANNEL));

    LogHelper.logSenderDirectly("Démarrage...");

    initializeGuild();

    LogHelper.logSenderDirectly("Chargement des champions ...");
    if(Main.loadChampions()) {
      LogHelper.logSenderDirectly("Chargement des champions terminé !");
    } else {
      LogHelper.logSenderDirectly("Une erreur est survenu lors du chargement des champions, les infos cards ne s'afficheront pas !");
    }

    LogHelper.logSenderDirectly("Chargement des emotes ...");

    try {
      loadCustomEmotes();
    } catch(IOException e) {
      logger.error("Erreur lors du chargment des emotes : {}", e.getMessage());
      LogHelper.logSenderDirectly("Erreur lors du chargement des emotes !");
    }

    LogHelper.logSenderDirectly("Chargement des emotes terminé !");

    LogHelper.logSenderDirectly("Chargement des sauvegardes détaillés...");

    try {
      Main.loadDataTxt();
    } catch(IOException e) {
      logger.error(e.getMessage());
      LogHelper.logSenderDirectly("Une erreur est survenu lors du chargement des sauvegardes détaillés !");
    } catch(RiotApiException e) {
      logger.error(e.getMessage());
      LogHelper.logSenderDirectly("Une erreur venant de l'api Riot est survenu lors du chargement des sauvegardes détaillés !");
    }

    LogHelper.logSenderDirectly("Chargement des sauvegardes détaillés terminé !");
    LogHelper.logSenderDirectly("Chargement des données des joueurs...");

    try {
      Main.loadPlayerDataWeek();
    } catch(IOException e) {
      logger.error(e.getMessage());
      LogHelper.logSenderDirectly("Une erreur est survenu lors du chagements des données joueurs !");
    }

    LogHelper.logSenderDirectly("Chargement des données des joueurs terminé !");
    LogHelper.logSenderDirectly("Démarrage de la partie musique Bot...");

    initMusicBot();

    LogHelper.logSenderDirectly("Démarrage de la partie musique Bot terminé !");
    LogHelper.logSenderDirectly("Connection à l'api Twitch...");

    initTwitchClient();

    LogHelper.logSenderDirectly("Connection à l'api Twitch effectué !");
    LogHelper.logSenderDirectly("Démarrage des tâches continue...");

    setupContinousRefreshThread();

    LogHelper.logSenderDirectly("Démarrage des tâches continues terminés !");

    Main.getJda().getPresence().setStatus(OnlineStatus.ONLINE);
    LogHelper.logSenderDirectly("Démarrage terminés !");
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    String message = event.getMessage().getContentRaw();

    List<Role> rolesOfSender = null;

    try {
      rolesOfSender = initializeRolesFromSender(event);
    } catch(NullPointerException e) {
      logger.info("L'envoyeur ne fait plus/pas parti du serveur");
      return;
    }

    boolean isAdmin = false;

    isAdmin = isAdminByRoles(rolesOfSender);

    if(event.getTextChannel().getId().equals(ID_POSTULATION_CHANNEL)
        && !PostulationCommand.getUserInRegistration().contains(event.getAuthor()) && !isAdmin
        && !Main.getJda().getSelfUser().equals(event.getAuthor())) {
      event.getMessage().delete().queue();

      PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
      privateChannel.sendTyping().queue();
      privateChannel.sendMessage(
          "On envoie uniquement des demandes de Postulation sur ce channel ! " + "(Note : Une postulation commence par \">postulation\")")
      .queue();

    }

    if(event.getTextChannel().getId().equals(ID_REPORT_CHANNEL) && !Main.getJda().getSelfUser().equals(event.getAuthor())) {

      event.getMessage().delete().complete();

      message = "Message de " + event.getAuthor().getName() + " :\n" + message;
      Main.addReport(message);

      PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
      privateChannel.sendMessage("Ton message a été envoyé, nous te répondrons dans les plus brefs délais !").queue();
    }

    if(message.length() == 0 || message.charAt(0) != PREFIX) {
      return;
    }

    message = message.substring(1);

    String command = message.split(" ")[0];

    if(isAdmin) {

      if(command.equalsIgnoreCase("delete")) {

        event.getTextChannel().sendTyping().complete();
        String result = CommandManagement.deleteCommand(message.substring(7));
        event.getTextChannel().sendMessage(result).queue();

      } else if(command.equalsIgnoreCase("accept")) {

        event.getTextChannel().sendTyping().complete();
        String result = CommandManagement.postulationAcceptCommand(Integer.parseInt(message.substring(7)), event.getAuthor());
        event.getTextChannel().sendMessage(result).queue();

      } else if(command.equalsIgnoreCase("clear")) {

        event.getTextChannel().sendTyping().complete();
        String result = CommandManagement.clearCommand(message.substring(6));
        event.getTextChannel().sendMessage(result).queue();

      } else if(command.equalsIgnoreCase("getAccountId")) {

        event.getTextChannel().sendTyping().complete();
        Summoner result;
        try {
          result = Ressources.getRiotApi().getSummonerByName(Platform.EUW, message.substring(13));
        } catch(RiotApiException e) {
          logger.error(e.toString());
          event.getTextChannel().sendMessage("Erreur d'api").queue();
          return;
        }
        event.getTextChannel().sendMessage("Account Id de " + result.getName() + " : " + result.getAccountId()).queue();
      }
    }
  }

  @Override
  public void onGuildJoin(GuildJoinEvent event) {

    List<CustomEmote> customeEmotesList = Main.getEmotesNeedToBeUploaded().poll();

    if(customeEmotesList == null) {
      logger.error("Pas d'emote à envoyer ! Suppression de la guild ...");

      if(event.getGuild().getOwner().getUser().equals(Main.getJda().getSelfUser())) {
        event.getGuild().delete().complete();
      }

    }else {

      try {
        sendAllEmotesInGuild(event, customeEmotesList);
      }catch(Exception e) {
        logger.warn("Error with emotes sending ! Guild will be deleted");
        logger.info("Some of emotes will be probably disable");
        event.getGuild().delete().queue();
        return;
      }

      try {
        updateFileSave(event.getGuild());
      } catch(IOException e) {
        logger.warn("Impossible to save the new Guild ! Guild will be deleted");
        logger.info("Some of emotes will be probably disable");
        event.getGuild().delete().queue();
        return;
      }

      Ressources.getCustomEmote().addAll(customeEmotesList);
      
      assigneEmotesToChampion();
      
      logger.info("New emote Guild \"{}\" initialized !", event.getGuild().getName());
    }
  }

  private synchronized void updateFileSave(Guild guildToAdd) throws IOException {
    List<Guild> emotesGuild = new ArrayList<>();

    try(BufferedReader reader = new BufferedReader(new FileReader(Main.GUILD_EMOTES_FILE));){
      int numberOfGuild;
      numberOfGuild = Integer.parseInt(reader.readLine());

      for(int i = 0; i < numberOfGuild; i++) {
        emotesGuild.add(Main.getJda().getGuildById(numberOfGuild));
      }
    }

    emotesGuild.add(guildToAdd);

    StringBuilder saveString = new StringBuilder();
    saveString.append(Integer.toString(emotesGuild.size()) + "\n");

    for(Guild guild : emotesGuild) {
      saveString.append(guild.getId() + "\n");
    }

    try(PrintWriter writer = new PrintWriter(Main.GUILD_EMOTES_FILE, "UTF-8");){
      writer.write(saveString.toString());
    }
  }

  private void sendAllEmotesInGuild(GuildJoinEvent event, List<CustomEmote> customeEmotesList) {
    GuildController guildController = event.getGuild().getController();

    for(CustomEmote customEmote : customeEmotesList) {
      try {
        Icon icon;
        icon = Icon.from(customEmote.getFile());

        Emote emote = guildController.createEmote(customEmote.getName(), icon, event.getGuild().getPublicRole()).complete();

        customEmote.setEmote(emote);
      } catch (IOException e) {
        logger.warn("Impossible de charger l'image !");
      }
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

  public static Message getStatusReportMessage() {
    return statusReportMessage;
  }
}
