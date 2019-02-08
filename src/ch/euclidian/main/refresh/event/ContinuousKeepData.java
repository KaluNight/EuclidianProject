package ch.euclidian.main.refresh.event;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Minutes;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ch.euclidian.main.Main;
import ch.euclidian.main.exception.NoValueRankException;
import ch.euclidian.main.model.ChangedStats;
import ch.euclidian.main.model.DatedFullTier;
import ch.euclidian.main.model.GraphData;
import ch.euclidian.main.model.KDA;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.model.PlayerDataOfTheWeek;
import ch.euclidian.main.model.StatsType;
import ch.euclidian.main.model.Team;
import ch.euclidian.main.util.LogHelper;
import ch.euclidian.main.util.Ressources;
import net.dv8tion.jda.core.entities.TextChannel;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.match.dto.MatchList;
import net.rithms.riot.api.endpoints.match.dto.MatchReference;
import net.rithms.riot.api.endpoints.match.dto.Participant;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class ContinuousKeepData implements Runnable {

  public static final String FOLDER_DATA_PLAYERS = "ressources/playersData/";
  
  private static final String DATE_PATTERN = "MM/dd";

  private static boolean running;

  private static DateTime weekDateStart;

  private static DateTime weekDateEnd;

  private static TextChannel statsChannel;

  private static ArrayList<String> messagesToSend;

  private static final Logger logger = LoggerFactory.getLogger(ContinuousKeepData.class);

  @Override
  public void run() {
    try {
      setRunning(true);

      statsChannel.sendTyping().complete();
      statsChannel.sendMessage("Je commence l'analyse de vos parties de la semaine, cela devrait me prendre quelques minutes").complete();

      setMessagesToSend(new ArrayList<>());

      messagesToSend.add("__**Rapports Hebdomadaire**__");

      for(int i = 0; i < Main.getPlayerList().size(); i++) {
        Player player = Main.getPlayerList().get(i);

        LogHelper.logSender("Construction du rapport de " + player.getName());

        Summoner summoner = player.getSummoner();

        String name = "";
        if(player.isMentionnable()) {
          name = player.getDiscordUser().getAsMention();
        } else {
          name = player.getDiscordUser().getName();
        }

        messagesToSend.add("**Rapport pour " + name + " sur le compte " + summoner.getName() + ".**");

        MatchList matchHistory = null;
        try {
          matchHistory = Ressources.getRiotApi().getMatchListByAccountId(Platform.EUW, summoner.getAccountId(), null, null, null,
              weekDateStart.getMillis(), weekDateEnd.getMillis(), -1, -1);
        } catch(RiotApiException e) {
          logger.debug(player.getDiscordUser().getName() + " ne possède pas de games pour la période envoyé");
        }

        if(matchHistory != null) {

          PlayerDataOfTheWeek playerDataOfTheWeek = getDataFromTheHistory(matchHistory, player.getSummoner());

          if(playerDataOfTheWeek != null) {
            List<PlayerDataOfTheWeek> savedDatasPlayer = Main.getPlayerList().get(i).getListDataOfWeek(); // Check if copy
            savedDatasPlayer.add(playerDataOfTheWeek);

            List<ChangedStats> changedStats = generatingStats(player);

            if(!changedStats.isEmpty()) {
              sendReport(player, changedStats);
            }
          } else {
            messagesToSend.add("Vos données n'ont pas pu être analysé D:");
          }
        } else {
          messagesToSend.add("Vous n'avez fait aucune partie cette semaine, je ne peux donc rien analyser :c");
        }
      }

      LogHelper.logSender("Analyse terminé, les rapports sont sauvegardés ...");

      try {
        saveData();
        LogHelper.logSender("Donnés sauvegardés ! Envoi des rapports personnels");
      } catch(IOException e) {
        LogHelper.logSender("Des logs on essayé d'être écrite alors que Writer était fermé ! Les données ne sont donc pas sauvegardés");
      }
      
      statsChannel.sendTyping().complete();
      statsChannel.sendMessage("Me revoilà !\nVoici vos rapports :D").complete();

      for(int i = 0; i < messagesToSend.size(); i++) {
        statsChannel.sendTyping().complete();
        statsChannel.sendMessage(messagesToSend.get(i)).complete();
      }
      
      LogHelper.logSender("Les rapports personnelles ont été envoyés ! Création des rapports d'équipes ...");
      
      statsChannel.sendTyping().complete();
      statsChannel.sendMessage("Je reviens dans quelques minutes avec cette fois-ci des stats par rapport au équipes !").complete();
      
      List<GraphData> graphDatas = generateGraphForTeam();;
      
      LogHelper.logSender("Analyse par équipe terminé ! Les rapports sont envoyés ...");
      
      for(GraphData graphData : graphDatas) {
        statsChannel.sendMessage(graphData.getMessageString()).complete();
        statsChannel.sendFile(graphData.getGraphData(), graphData.getGraphName()).complete();
      }

      LogHelper.logSender("Tous les rapports ont été envoyé !");

      setWeekDateEnd(weekDateEnd.plusWeeks(1));
      setWeekDateStart(weekDateStart.plusWeeks(1));

      statsChannel.sendTyping().complete();
      statsChannel.sendMessage("Le prochain rapport que je ferai sera le **" + weekDateEnd.getDayOfMonth() + "."
          + weekDateEnd.getMonthOfYear() + "." + weekDateEnd.getYear() + " à " + weekDateEnd.getHourOfDay() + ":"
          + weekDateEnd.getMinuteOfHour() + "**.\nPassez une bonne journée !").complete();

      setMessagesToSend(new ArrayList<>());

    } finally {
      setRunning(false);
    }
  }

  private List<GraphData> generateGraphForTeam() {
    List<GraphData> graphDatas = new ArrayList<>();
    
    for(Team team : Main.getTeamList()) {
      List<List<DatedFullTier>> datedFullTier = new ArrayList<>();
      List<String> listPseudo = new ArrayList<>();
      
      for(Player player : team.getPlayers()) {
        try {
          datedFullTier.add(Ressources.loadTierOnePlayer(player.getDiscordUser().getId()));
          listPseudo.add(player.getDiscordUser().getName());
        } catch (FileNotFoundException e) {
          logger.info("{} ne possède pas de fichier de Tier", player.getName());
        }
      }
      
      XYChart chartTierTeam = createChart(datedFullTier, team.getName(), listPseudo);
      
      byte[] chartPicture = null;
      
      try {
        chartPicture = BitmapEncoder.getBitmapBytes(chartTierTeam, BitmapFormat.PNG);
      } catch (IOException e) {
        logger.warn("Impossible de créer le graph de ranked pour l'équipe {} : {}", team.getName(), e.getMessage());
      }
      
      if(chartPicture != null) {
        GraphData graphData = new GraphData(
            "Graph de ranked pour l'équipe " + team.getRole().getAsMention(),
            "RankedGraph" + team.getName() + ".png",
            chartPicture);
        
        graphDatas.add(graphData);
      }
    }
    return graphDatas;
  }
  
  private XYChart createChart(List<List<DatedFullTier>> datedFullTier, String teamName, List<String> memberName) {
    XYChartBuilder chartBuilder = new XYChartBuilder();
    
    chartBuilder.title = "Rangs de la " + teamName;
    chartBuilder.chartTheme = ChartTheme.GGPlot2;

    chartBuilder.xAxisTitle("Jours");
    chartBuilder.yAxisTitle("Rangs");
    
    XYChart chart = chartBuilder.build();
    
    
    for(int i = 0; i < datedFullTier.size(); i++) {
      String actualName = memberName.get(i);
      List<DatedFullTier> actualTierData = datedFullTier.get(i);
      
      List<Number> valueData = new ArrayList<>();

      for(int j = 0; j < actualTierData.size(); j++) {
        try {
          valueData.add(actualTierData.get(j).getFullTier().value());
        } catch (NoValueRankException e) {
          valueData.add(null);
        }
      }
      
      List<Date> dateData = new ArrayList<>();

      for(int j = 0; j < actualTierData.size(); j++) {
        dateData.add(actualTierData.get(j).getCreationTime().toDate());
      }
      chart.addSeries(actualName, dateData, valueData);
    }
    
    chart.setYAxisLabelOverrideMap(Ressources.getTableCorrespondanceRank());
    chart.getStyler().setDatePattern(DATE_PATTERN);
    
    chart.getStyler().setAntiAlias(true);
    
    return chart;
  }

  private void saveData() throws IOException {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    for(int i = 0; i < Main.getPlayerList().size(); i++) {
      List<PlayerDataOfTheWeek> dataPlayer = Main.getPlayerList().get(i).getListDataOfWeek();

      Writer writer = null;
      try {
        writer = new FileWriter(FOLDER_DATA_PLAYERS + Main.getPlayerList().get(i).getDiscordUser().getId() + ".json");

        gson.toJson(dataPlayer, writer);
      } catch(IOException e) {
        LogHelper.logSender(Main.getPlayerList().get(i).getDiscordUser().getName() + " n'a pas pu être enregistré");
      } finally {
        if(writer != null) {
          writer.close();
        }
      }
    }
  }

  private void sendReport(Player player, List<ChangedStats> changedStats) {

    for(int i = 0; i < changedStats.size(); i++) {
      ChangedStats stats = changedStats.get(i);
      messagesToSend.add("**" + stats.getType().toString() + "**\n" + stats.toString());
    }
  }

  private List<ChangedStats> generatingStats(Player player) {
    // TODO: check value different value with ChangedStats
    if(player.getListDataOfWeek().size() == 1) {
      messagesToSend.add("C'est la première fois que vos donnés sont analysé, vous aurez un rapport la semaine prochaine.");
      return new ArrayList<>();
    } else if(player.getListDataOfWeek().isEmpty()) {
      messagesToSend.add("Vos données n'ont pas pu être analysé normalement, un dev va s'occuper de votre cas :x");
      return new ArrayList<>();
    } else {
      PlayerDataOfTheWeek lastWeek = player.getListDataOfWeek().get(player.getListDataOfWeek().size() - 1);
      PlayerDataOfTheWeek thisWeek = player.getListDataOfWeek().get(player.getListDataOfWeek().size() - 2);

      ArrayList<ChangedStats> listOfChangedStats = new ArrayList<>();

      listOfChangedStats
          .add(new ChangedStats(StatsType.DURATION, lastWeek.getAverageDurationOfTheWeek(), thisWeek.getAverageDurationOfTheWeek()));
      listOfChangedStats.add(new ChangedStats(StatsType.CREEP_AT_10, lastWeek.getAverageCreepsAt10(), thisWeek.getAverageCreepsAt10()));
      listOfChangedStats.add(new ChangedStats(StatsType.CREEP_AT_20, lastWeek.getAverageCreepsAt20(), thisWeek.getAverageCreepsAt20()));
      listOfChangedStats.add(new ChangedStats(StatsType.CREEP_AT_30, lastWeek.getAverageCreepsAt30(), thisWeek.getAverageCreepsAt30()));
      listOfChangedStats.add(new ChangedStats(StatsType.KDA, lastWeek.getKDAOfTheWeek(), thisWeek.getKDAOfTheWeek()));

      return listOfChangedStats;
    }
  }

  private PlayerDataOfTheWeek getDataFromTheHistory(MatchList matchHistory, Summoner summoner) {
    ArrayList<Duration> listeDuration = new ArrayList<>();
    ArrayList<Double> listTotCreep10Minute = new ArrayList<>();
    ArrayList<Double> listTotCreep20Minute = new ArrayList<>();
    ArrayList<Double> listTotCreep30Minute = new ArrayList<>();
    ArrayList<Integer> listOfSummonerSpellUsed = new ArrayList<>();
    ArrayList<KDA> listOfKDA = new ArrayList<>();
    ArrayList<Integer> listOfChampionPlayed = new ArrayList<>();

    int nbrGames = 0;
    int nbrWin = 0;

    List<MatchReference> matchList = matchHistory.getMatches();

    for(int i = 0; i < matchHistory.getTotalGames(); i++) {
      MatchReference matchReference = matchList.get(i);

      Match match = null;
      try {
        match = Ressources.getRiotApi().getMatch(Platform.EUW, matchReference.getGameId());
      } catch(RiotApiException e) {
        logger.error(e.getMessage());
        return null;
      }

      Participant participant = match.getParticipantBySummonerId(summoner.getId());

      if(participant.getTimeline().getCreepsPerMinDeltas() != null) {

        Duration matchLength = new Duration(match.getGameDuration() * 1000);

        listeDuration.add(matchLength);

        Minutes minutes = matchLength.toStandardMinutes();

        if(participant.getTimeline().getCreepsPerMinDeltas().get("0-10") != null) {
          listTotCreep10Minute.add(participant.getTimeline().getCreepsPerMinDeltas().get("0-10") * 10);
        }

        if(participant.getTimeline().getCreepsPerMinDeltas().get("10-20") != null) {
          listTotCreep20Minute.add(participant.getTimeline().getCreepsPerMinDeltas().get("10-20") * 10);
        }

        if(minutes.getMinutes() >= 30) {
          listTotCreep30Minute.add(participant.getTimeline().getCreepsPerMinDeltas().get("20-30") * 10);
        }

        listOfSummonerSpellUsed.add(participant.getSpell1Id());
        listOfSummonerSpellUsed.add(participant.getSpell2Id());

        int kills = participant.getStats().getKills();
        int deaths = participant.getStats().getDeaths();
        int assists = participant.getStats().getAssists();
        listOfKDA.add(new KDA(kills, deaths, assists));

        listOfChampionPlayed.add(participant.getChampionId());

        String result = match.getTeamByTeamId(participant.getTeamId()).getWin();
        if(result.equalsIgnoreCase("Win") || result.equalsIgnoreCase("Fail")) {
          nbrGames++;
        }

        if(result.equalsIgnoreCase("Win")) {
          nbrWin++;
        }
      }
    }

    PlayerDataOfTheWeek playerDataOfTheWeek = new PlayerDataOfTheWeek(weekDateStart.toString(), weekDateEnd.toString());

    playerDataOfTheWeek.setListeDuration(listeDuration);
    playerDataOfTheWeek.setListOfChampionPlayed(listOfChampionPlayed);
    playerDataOfTheWeek.setListOfKDA(listOfKDA);
    playerDataOfTheWeek.setListOfSummonerSpellUsed(listOfSummonerSpellUsed);
    playerDataOfTheWeek.setListTotCreep10Minute(listTotCreep10Minute);
    playerDataOfTheWeek.setListTotCreep20Minute(listTotCreep20Minute);
    playerDataOfTheWeek.setListTotCreep30Minute(listTotCreep30Minute);
    playerDataOfTheWeek.setNbrGames(nbrGames);
    playerDataOfTheWeek.setNbrWin(nbrWin);

    return playerDataOfTheWeek;
  }

  public static DateTime getWeekDateStart() {
    return weekDateStart;
  }

  public static DateTime getWeekDateEnd() {
    return weekDateEnd;
  }

  public static TextChannel getStatsChannel() {
    return statsChannel;
  }

  public static void setWeekDateStart(DateTime weekDateStart) {
    ContinuousKeepData.weekDateStart = weekDateStart;
  }

  public static void setWeekDateEnd(DateTime weekDateEnd) {
    ContinuousKeepData.weekDateEnd = weekDateEnd;
  }

  public static void setStatsChannel(TextChannel statsChannel) {
    ContinuousKeepData.statsChannel = statsChannel;
  }

  public static ArrayList<String> getMessagesToSend() {
    return messagesToSend;
  }

  public static void setMessagesToSend(ArrayList<String> messagesToSend) {
    ContinuousKeepData.messagesToSend = messagesToSend;
  }

  public static boolean isRunning() {
    return running;
  }

  public static void setRunning(boolean running) {
    ContinuousKeepData.running = running;
  }
}
