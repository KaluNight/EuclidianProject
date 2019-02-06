package ch.euclidian.main.model.discord.command;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.euclidian.main.Main;
import ch.euclidian.main.exception.NoValueRankException;
import ch.euclidian.main.model.DatedFullTier;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.util.Ressources;
import net.dv8tion.jda.core.entities.User;

public class TierChartPlayerCommand extends Command {

  private static final Logger logger = LoggerFactory.getLogger(Ressources.class);

  public TierChartPlayerCommand() {
    this.name = "playerGraph";
    this.arguments = "MentionJoueur";
    this.help = "Crée un graphique avec les données d'un joueurs";
  }

  @Override
  protected void execute(CommandEvent event) {

    event.getTextChannel().sendTyping().complete();
    List<User> mentionnedUser = event.getMessage().getMentionedUsers();

    if(mentionnedUser.isEmpty()) {
      event.reply("Vous devez mentionner un joueur !");
      return;
    }else if(mentionnedUser.size() > 1) {
      event.reply("Vous ne pouvez pas mentionner plusieurs personnes !");
      return;
    }

    User user = mentionnedUser.get(0);
    Player player = Main.getPlayersByDiscordId(user.getId());

    if(player == null) {
      event.reply("La personne mentionné n'est pas enregistré dans le système");
      return;
    }

    List<DatedFullTier> datedFullTier;
    try {
      datedFullTier = Ressources.loadTierOnePlayer(user.getId());
    } catch (FileNotFoundException e) {
      logger.info("L'utilisateur ne possède pas de fichier de Tier");
      event.reply("Je n'ai pas encore collecté de données");
      return;
    }

    if(datedFullTier.size() < 4) {
      event.reply("Je n'ai pas encore récolté suffisament de données sur ce joueur, réessayez dans " + (datedFullTier.size() - 3) + " jours");
      return;
    }
    
    XYChart chart = createChart(datedFullTier, user.getName());
    
    byte[] chartPicture;
    
    try {
      chartPicture = BitmapEncoder.getBitmapBytes(chart, BitmapFormat.PNG);
    } catch (IOException e) {
      event.reply("Erreur dans la sauvegarde du graphique");
      return;
    }
    
    event.getTextChannel().sendFile(chartPicture, "Votre graphique de ranked :").complete();
  }

  private XYChart createChart(List<DatedFullTier> datedFullTier, String playerName) {

    List<Integer> valueData = new ArrayList<>();

    for(int i = 0; i < datedFullTier.size(); i++) {
      try {
        valueData.add(datedFullTier.get(i).getFullTier().value());
      } catch (NoValueRankException e) {
        valueData.add(null);
      }
    }
    
    List<String> dateData = new ArrayList<>();

    for(int i = 0; i < datedFullTier.size(); i++) {
      DateTime creationTime = datedFullTier.get(i).getCreationTime();
      
      dateData.add(creationTime.getDayOfMonth() + "." + creationTime.getMonthOfYear());
    }
    
    XYChartBuilder chartBuilder = new XYChartBuilder();
    
    chartBuilder.title = "Classement en ranked par jour";
    chartBuilder.chartTheme = ChartTheme.GGPlot2;
    chartBuilder.height = 400;
    chartBuilder.width = 600;
    
    chartBuilder.xAxisTitle("Jours");
    chartBuilder.yAxisTitle("Rangs");
    
    XYChart chart = chartBuilder.build();
    
    chart.addSeries(playerName, dateData, valueData);
    
    Map<Double, Object> yMarkMap = new TreeMap<>();
    yMarkMap.put(1000.0, "Fer 4");
    yMarkMap.put(1100.0, "Fer 3");
    yMarkMap.put(1200.0, "Fer 2");
    yMarkMap.put(1300.0, "Fer 1");
    yMarkMap.put(1400.0, "Bronze 4");
    yMarkMap.put(1500.0, "Bronze 3");
    yMarkMap.put(1600.0, "Bronze 2");
    yMarkMap.put(1700.0, "Bronze 1");
    yMarkMap.put(1800.0, "Argent 4");
    yMarkMap.put(1900.0, "Argent 3");
    yMarkMap.put(2000.0, "Argent 2");
    yMarkMap.put(2100.0, "Argent 1");
    yMarkMap.put(2200.0, "Or 4");
    yMarkMap.put(2300.0, "Or 3");
    yMarkMap.put(2400.0, "Or 2");
    yMarkMap.put(2500.0, "Or 1");
    yMarkMap.put(2600.0, "Platine 4");
    yMarkMap.put(2700.0, "Platine 3");
    yMarkMap.put(2800.0, "Platine 2");
    yMarkMap.put(2900.0, "Platine 1");
    yMarkMap.put(3000.0, "Diamant 4");
    yMarkMap.put(3100.0, "Diamant 3");
    yMarkMap.put(3200.0, "Diamant 2");
    yMarkMap.put(3300.0, "Diamant 1");
    yMarkMap.put(3400.0, "Master+");
    
    chart.setYAxisLabelOverrideMap(yMarkMap);
    
    chart.getStyler().setAntiAlias(true);
    
    return chart;
  }


}
