package ch.euclidian.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import ch.euclidian.main.model.Champion;
import ch.euclidian.main.model.CustomEmote;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.model.PlayerDataOfTheWeek;
import ch.euclidian.main.model.Postulation;
import ch.euclidian.main.model.Team;
import ch.euclidian.main.model.discord.command.AddPlayerToTeamCommand;
import ch.euclidian.main.model.discord.command.PingCommand;
import ch.euclidian.main.model.discord.command.PostulationCommand;
import ch.euclidian.main.model.discord.command.RegisterPlayerCommand;
import ch.euclidian.main.model.discord.command.ShowCommand;
import ch.euclidian.main.model.discord.command.ShutDownCommand;
import ch.euclidian.main.model.discord.command.TierChartPlayerCommand;
import ch.euclidian.main.model.discord.command.create.CreateTeamCommand;
import ch.euclidian.main.model.discord.command.music.LeaveCommand;
import ch.euclidian.main.model.discord.command.music.PlayCommand;
import ch.euclidian.main.model.discord.command.music.ResetCommand;
import ch.euclidian.main.model.discord.command.music.SkipCommand;
import ch.euclidian.main.refresh.event.ContinuousKeepData;
import ch.euclidian.main.refresh.event.ContinuousTimeChecking;
import ch.euclidian.main.util.LogHelper;
import ch.euclidian.main.util.Ressources;
import ch.euclidian.main.util.SleeperRateLimitHandler;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.managers.GuildController;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.api.request.ratelimit.RateLimitHandler;
import net.rithms.riot.constant.Platform;

public class Main {

  private static File SAVE_TXT_FILE = new File("ressources/save.txt");

  private static final File GUILD_EMOTES_FILE = new File("ressources/guilds.txt");

  private static final int MAX_EMOTE_BY_GUILD = 50;

  private static final File SECRET_FILE = new File("secret.txt");
  
  private static final ConcurrentLinkedQueue<List<CustomEmote>> emotesNeedToBeUploaded = new ConcurrentLinkedQueue<>();

  // ------------------------------

  private static JDA jda;

  // -------------------------------

  private static ArrayList<Team> teamList = new ArrayList<>();

  private static ArrayList<Player> playerList = new ArrayList<>();

  private static ArrayList<Postulation> postulationsList = new ArrayList<>();

  private static ArrayList<String> reportList = new ArrayList<>();

  private static Role registeredRole;

  private static Role postulantRole;

  private static ArrayList<Role> rolePosition;

  // -------------------------------

  private static Guild guild;

  private static GuildController controller;

  // -------------------------------

  private static TextChannel logBot;

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    System.setProperty("logback.configurationFile", "logback.xml");

    String discordTocken = "";
    String riotTocken = "";
    String twitchClientID = "";
    String twitchClientSecret = "";
    String twitchCredential = "";

    if(args.length == 0) {

      try(BufferedReader reader = new BufferedReader(new FileReader(SECRET_FILE));) {
        discordTocken = reader.readLine();
        riotTocken = reader.readLine();
        twitchClientID = reader.readLine();
        twitchClientSecret = reader.readLine();
        twitchCredential = reader.readLine();
      } catch(Exception e) {
        logger.error(e.getMessage());
      }

      SAVE_TXT_FILE = new File("save.txt");

    } else {
      discordTocken = args[0];
      riotTocken = args[1];
      twitchClientID = args[2];
      twitchClientSecret = args[3];
      twitchCredential = args[4];
    }

    Ressources.setTwitchClientId(twitchClientID);
    Ressources.setTwitchClientSecret(twitchClientSecret);
    Ressources.setTwitchCredential(twitchCredential);

    EventWaiter waiter = new EventWaiter();

    CommandClientBuilder client = new CommandClientBuilder();

    client.setPrefix(">");

    client.setEmojis("\uD83D\uDE03", "\uD83D\uDE2E", "\uD83D\uDE26");

    client.setOwnerId("228541163966038016");

    client.setHelpConsumer(null); // TODO: Set a command

    client.setGame(Game.playing("Démarrage ..."));

    client.addCommands(
        // General Command
        new PingCommand(), new ShutDownCommand(),

        // Guild Command
        new PostulationCommand(waiter), new RegisterPlayerCommand(), new CreateTeamCommand(), new AddPlayerToTeamCommand(),
        new ShowCommand(), new TierChartPlayerCommand(),

        // Music Command
        new LeaveCommand(), new PlayCommand(), new ResetCommand(), new SkipCommand());

    try {
      jda = new JDABuilder(AccountType.BOT)//
          .setToken(discordTocken)//
          .setStatus(OnlineStatus.DO_NOT_DISTURB)//
          .addEventListener(waiter)//
          .addEventListener(client.build())//
          .addEventListener(new EventListener())//
          .build();//
    } catch(IndexOutOfBoundsException e) {
      logger.error("You must provide a token.");
      return;
    } catch(Exception e) {
      logger.error(e.getMessage());
      return;
    }

    ApiConfig config = new ApiConfig().setKey(riotTocken);

    RateLimitHandler defaultLimite = new SleeperRateLimitHandler();

    config.setRateLimitHandler(defaultLimite);
    Ressources.setRiotApi(new RiotApi(config));

    // print internal state
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    StatusPrinter.print(lc);
  }

  public static boolean loadChampions() {
    JsonParser parser = new JsonParser();
    List<Champion> champions = new ArrayList<>();

    try(FileReader fr = new FileReader("ressources/champion.json")) {

      JsonObject object = parser.parse(fr).getAsJsonObject().get("data").getAsJsonObject();
      Set<Map.Entry<String, JsonElement>> list = object.entrySet();
      Iterator<Map.Entry<String, JsonElement>> iterator = list.iterator();

      while(iterator.hasNext()) {
        JsonElement element = iterator.next().getValue();
        int key = element.getAsJsonObject().get("key").getAsInt();
        String id = element.getAsJsonObject().get("id").getAsString();
        String name = element.getAsJsonObject().get("name").getAsString();
        File championLogo =
            new File("ressources/images/" + element.getAsJsonObject().get("image").getAsJsonObject().get("full").getAsString());
        champions.add(new Champion(key, id, name, championLogo));
      }

    } catch(IOException e) {
      logger.error(e.getMessage());
      return false;
    }

    Ressources.setChampions(champions);
    return true;
  }

  public static synchronized void loadPlayerDataWeek() throws IOException {
    Gson gson = new Gson();

    for(int i = 0; i < Main.getPlayerList().size(); i++) {
      try(FileReader fr =
          new FileReader(ContinuousKeepData.FOLDER_DATA_PLAYERS + Main.getPlayerList().get(i).getDiscordUser().getId() + ".json");) {

        List<PlayerDataOfTheWeek> playerData = gson.fromJson(fr, new TypeToken<List<PlayerDataOfTheWeek>>() {}.getType());

        Main.getPlayerList().get(i).setListDataOfWeek(playerData);
      } catch(JsonSyntaxException | JsonIOException e) {
        LogHelper.logSender("Le fichier de sauvegarde de " + Main.getPlayerList().get(i).getName() + " est corrompu !");
      } catch(FileNotFoundException e) {
        logger.info((Main.getPlayerList().get(i).getName() + " ne possède pas de sauvegarde de donnés"));
      }
    }
  }

  public static synchronized void saveDataTxt() throws FileNotFoundException, UnsupportedEncodingException {

    StringBuilder saveString = new StringBuilder();

    saveString.append("//Liste Of Player\n\n");

    for(int i = 0; i < playerList.size(); i++) {
      Player player = playerList.get(i);

      saveString.append("--p\n");

      saveString.append(player.getName() + "\n");
      saveString.append(player.getDiscordUser().getId() + "\n");
      saveString.append(player.getSummoner().getAccountId() + "\n");
      saveString.append(player.isMentionnable() + "\n\n");
    }

    saveString.append("\n//Liste of teams\n");

    for(int i = 0; i < teamList.size(); i++) {
      Team team = teamList.get(i);

      saveString.append("--t\n");

      saveString.append(team.getName() + "\n");
      saveString.append(team.getRole().getId() + "\n");
      saveString.append(team.getCategory().getId() + "\n");
      saveString.append(team.getPlayers().size() + "\n");

      for(int j = 0; j < team.getPlayers().size(); j++) {
        saveString.append(team.getPlayers().get(j).getDiscordUser().getId() + "\n");
      }
      saveString.append("\n");
    }

    saveString.append("\n\n//Postulations\n");

    for(int i = 0; i < postulationsList.size(); i++) {
      Postulation postulation = postulationsList.get(i);

      saveString.append("--post\n");

      saveString.append(postulation.getMember().getUser().getId() + "\n");
      saveString.append(postulation.getSummoner().getAccountId() + "\n");
      saveString.append(postulation.getRoles().size() + "\n");

      for(int j = 0; j < postulation.getRoles().size(); j++) {
        saveString.append(postulation.getRoles().get(j).getId() + "\n");
      }

      saveString.append(postulation.getHoraires());
      saveString.append("\n--end");

      saveString.append("\n");
    }

    saveString.append("\n\n//Report\n");

    for(int i = 0; i < reportList.size(); i++) {
      saveString.append("--r\n");

      saveString.append(reportList.get(i));
      saveString.append("--end\n");
    }

    saveString.append("\n\n//StatsDate\n");
    saveString.append("--st\n");

    saveString.append(ContinuousKeepData.getStatsChannel().getId() + "\n");
    saveString.append(ContinuousKeepData.getWeekDateStart().toString() + "\n");
    saveString.append(ContinuousKeepData.getWeekDateEnd().toString() + "\n");

    PrintWriter writer = null;

    try {
      writer = new PrintWriter(SAVE_TXT_FILE, "UTF-8");
      writer.write(saveString.toString());
    } finally {
      if(writer != null) {
        writer.close();
      }
    }
  }

  public static void loadDataTxt() throws IOException, RiotApiException {
    BufferedReader reader = null;

    try {
      reader = new BufferedReader(new FileReader(SAVE_TXT_FILE));
      String line;

      while((line = reader.readLine()) != null) {

        if(line.equals("--p")) {
          String discordName = reader.readLine();
          String discordID = reader.readLine();
          String accountId = reader.readLine();
          boolean mentionable = Boolean.parseBoolean(reader.readLine());

          if(!isPlayersAlreadyCopied(discordID)) {
            User user = jda.getUserById(discordID);
            Summoner summoner = Ressources.getRiotApi().getSummonerByAccount(Platform.EUW, accountId);

            if(user != null && summoner != null) {
              playerList.add(new Player(discordName, user, summoner, mentionable));
            }
          }

        } else if(line.equals("--t")) {

          String teamName = reader.readLine();

          String roleId = reader.readLine();
          Role role = guild.getRoleById(roleId);


          String categoryId = reader.readLine();
          Category category = guild.getCategoryById(categoryId);

          Team team = new Team(teamName, category, role);

          int numberOfPlayer = Integer.parseInt(reader.readLine());

          ArrayList<Player> players = new ArrayList<Player>();
          for(int i = 0; i < numberOfPlayer; i++) {
            String id = reader.readLine();
            Player player = getPlayersByDiscordId(id);

            if(player != null) {
              players.add(player);
            }

          }

          team.setPlayers(players);
          teamList.add(team);

        } else if(line.equals("--st")) {

          String idChannel = reader.readLine();
          ContinuousKeepData.setStatsChannel(guild.getTextChannelById(idChannel));

          String millisStart = reader.readLine();
          ContinuousKeepData.setWeekDateStart(DateTime.parse(millisStart));

          String millisEnd = reader.readLine();
          ContinuousKeepData.setWeekDateEnd(DateTime.parse(millisEnd));
          ContinuousTimeChecking.setNextTimeSendReport(DateTime.parse(millisEnd));

        } else if(line.equals("--post")) {

          String userId = reader.readLine();
          Member member = guild.getMemberById(userId);

          if(member != null) {

            Summoner summoner = Ressources.getRiotApi().getSummonerByAccount(Platform.EUW, reader.readLine());

            ArrayList<Role> roles = new ArrayList<>();

            int roleNmbr = Integer.parseInt(reader.readLine());
            for(int j = 0; j < roleNmbr; j++) {
              String roleId = reader.readLine();
              roles.add(guild.getRoleById(roleId));
            }

            StringBuilder horaires = new StringBuilder();

            String actualLine = reader.readLine();
            String nextLine = reader.readLine();

            int i = 0;

            while(true) {
              if(i != 0) {
                actualLine = nextLine;
                nextLine = reader.readLine();
              }
              i++;

              if(actualLine.equals("--end")) {
                break;
              } else if(nextLine.equals("--end")) {
                horaires.append(actualLine);
              } else {
                horaires.append(actualLine + "\n");
              }
            }

            Postulation postulation = new Postulation(member, summoner, roles, horaires.toString());
            postulationsList.add(postulation);
          }

        } else if(line.equals("--r")) {

          StringBuilder stringBuilder = new StringBuilder();

          while(true) {
            line = reader.readLine();

            if(line.equals("--stop")) {
              break;
            } else {
              stringBuilder.append(line);
            }
          }

          reportList.add(stringBuilder.toString());
        }
      }

    } finally {
      reader.close();
    }
  }

  public static List<CustomEmote> loadPicturesInFile() {
    List<CustomEmote> emotes = new ArrayList<>();

    File folder = new File(Ressources.FOLDER_TO_EMOTES);
    File[] listOfFiles = folder.listFiles();

    for(int i = 0; i < listOfFiles.length; i++) {
      String name = listOfFiles[i].getName();
      if(name.endsWith(".png") || name.endsWith(".jpg")) {
        name = name.substring(0, name.length() - 4);
        emotes.add(new CustomEmote(name, listOfFiles[i]));
      }
    }
    return emotes;
  }

  public static List<CustomEmote> prepareUploadOfEmotes(List<CustomEmote> customEmotes) throws IOException {

    List<Guild> emoteGuilds = getEmoteGuilds();

    uploadEmoteInGuildAlreadyExist(customEmotes, emoteGuilds);

    int j = 0;
    
    while(!customEmotes.isEmpty()) {
      j++;
      jda.createGuild("ZoeTrainer Emotes Guild " + emoteGuilds.size() + j).complete(); //TODO HANDLE GUILD JOIN EVENT
      
      List<CustomEmote> listEmoteForActualGuild = new ArrayList<>();

      for(int i = 0; i < MAX_EMOTE_BY_GUILD; i++) {
        if(customEmotes.isEmpty()) {
          break;
        }
        
        listEmoteForActualGuild.add(customEmotes.get(0));
        customEmotes.remove(0);
      }
      
      emotesNeedToBeUploaded.add(listEmoteForActualGuild);
    }
    return customEmotes;
  }

  private static List<CustomEmote> uploadEmoteInGuildAlreadyExist(List<CustomEmote> customEmotes, List<Guild> emoteGuilds) throws IOException {
    List<CustomEmote> emotesUploaded = new ArrayList<>();

    for(Guild guild : emoteGuilds) {
      List<Emote> emotes = getNonAnimatedEmoteOfTheGuild(guild);

      GuildController guildController = guild.getController();

      int emotesSize = emotes.size();
      
      while(emotesSize < MAX_EMOTE_BY_GUILD) {
        CustomEmote customEmote = customEmotes.get(0);
        Icon icon = Icon.from(customEmote.getFile());
        Emote emote = guildController.createEmote(customEmote.getName(), icon, guild.getPublicRole()).complete();
        
        emotesSize++;
        
        customEmote.setEmote(emote);
        emotesUploaded.add(customEmote);
        customEmotes.remove(0);
      }
    }
    return emotesUploaded;
  }

  private static List<Emote> getNonAnimatedEmoteOfTheGuild(Guild guild) {
    List<Emote> emotes = guild.getEmotes();

    List<Emote> emotesNonAnimated = new ArrayList<>();
    for(Emote emote : emotes) {
      if(!emote.isAnimated()) {
        emotesNonAnimated.add(emote);
      }
    }
    return emotes;
  }

  private static List<Guild> getEmoteGuilds() throws IOException, FileNotFoundException {
    List<Guild> emoteGuild = new ArrayList<>();

    try(BufferedReader reader = new BufferedReader(new FileReader(GUILD_EMOTES_FILE));){
      int numberOfGuild = Integer.parseInt(reader.readLine());

      for(int i = 0; i < numberOfGuild; i++) {
        emoteGuild.add(jda.getGuildById(numberOfGuild));
      }
    }
    return emoteGuild;
  }

  private static boolean isPlayersAlreadyCopied(String discordUserId) {
    for(int i = 0; i < playerList.size(); i++) {
      if(playerList.get(i).getDiscordUser().getId().equals(discordUserId)) {
        return true;
      }
    }
    return false;
  }

  public static int getPostulationIndexByMember(Member member) {
    for(int i = 0; i < postulationsList.size(); i++) {
      if(postulationsList.get(i).getMember().equals(member)) {
        return i;
      }
    }
    return -1;
  }

  public static Role getPositionRoleByName(String str) {
    for(int i = 0; i < rolePosition.size(); i++) {
      if(rolePosition.get(i).getName().equalsIgnoreCase(str)) {
        return rolePosition.get(i);
      }
    }
    return null;
  }

  public static Player getPlayersByDiscordId(String id) {
    for(int i = 0; i < playerList.size(); i++) {
      if(playerList.get(i).getDiscordUser().getId().equals(id)) {
        return playerList.get(i);
      }
    }
    return null;
  }

  public static Player getPlayerBySummonerName(String summonerName) {
    for(int i = 0; i < playerList.size(); i++) {
      if(playerList.get(i).getSummoner().getName().equals(summonerName)) {
        return playerList.get(i);
      }
    }
    return null;
  }

  public static Team getTeamByName(String name) {
    for(int i = 0; i < teamList.size(); i++) {
      if(teamList.get(i).getName().equalsIgnoreCase(name)) {
        return teamList.get(i);
      }
    }
    return null;
  }

  public static void addReport(String report) {
    reportList.add(report);
  }

  public static JDA getJda() {
    return jda;
  }

  public static void setJda(JDA jda) {
    Main.jda = jda;
  }

  public static ArrayList<Team> getTeamList() {
    return teamList;
  }

  public static void setTeamList(ArrayList<Team> teamList) {
    Main.teamList = teamList;
  }

  public static ArrayList<Player> getPlayerList() {
    return playerList;
  }

  public static void setPlayerList(ArrayList<Player> playerList) {
    Main.playerList = playerList;
  }

  public static Guild getGuild() {
    return guild;
  }

  public static void setGuild(Guild guild) {
    Main.guild = guild;
  }

  public static GuildController getController() {
    return controller;
  }

  public static void setController(GuildController controller) {
    Main.controller = controller;
  }

  public static Role getRegisteredRole() {
    return registeredRole;
  }

  public static void setRegisteredRole(Role registeredRole) {
    Main.registeredRole = registeredRole;
  }

  public static ArrayList<Role> getRolePosition() {
    return rolePosition;
  }

  public static void setRolePosition(ArrayList<Role> rolePosition) {
    Main.rolePosition = rolePosition;
  }

  public static ArrayList<Postulation> getPostulationsList() {
    return postulationsList;
  }

  public static void setPostulationsList(ArrayList<Postulation> postulationsList) {
    Main.postulationsList = postulationsList;
  }

  public static Role getPostulantRole() {
    return postulantRole;
  }

  public static void setPostulantRole(Role postulantRole) {
    Main.postulantRole = postulantRole;
  }

  public static TextChannel getLogBot() {
    return logBot;
  }

  public static void setLogBot(TextChannel logBot) {
    Main.logBot = logBot;
  }

  public static ArrayList<String> getReportList() {
    return reportList;
  }

  public static void setReportList(ArrayList<String> reportList) {
    Main.reportList = reportList;
  }
  
  public static ConcurrentLinkedQueue<List<CustomEmote>> getEmotesNeedToBeUploaded() {
    return emotesNeedToBeUploaded;
  }
}
