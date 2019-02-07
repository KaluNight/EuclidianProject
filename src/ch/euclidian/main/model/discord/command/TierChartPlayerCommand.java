package ch.euclidian.main.model.discord.command;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
  
  private static final String DATE_PATTERN = "MM/dd";

  private static final Logger logger = LoggerFactory.getLogger(Ressources.class);

  public TierChartPlayerCommand() {
    this.name = "playerGraph";
    this.arguments = "MentionJoueur";
    this.help = "Crée un graphique avec les données d'un joueur";
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
    
    XYChart chart;
    try {
      chart = createChart(datedFullTier, user.getName());
    } catch (NoValueRankException e) {
      event.reply(e.getMessage());
      return;
    }
    
    byte[] chartPicture;
    
    try {
      chartPicture = BitmapEncoder.getBitmapBytes(chart, BitmapFormat.PNG);
    } catch (IOException e) {
      event.reply("Erreur dans la création du graphique");
      return;
    }
    
    event.getTextChannel().sendMessage("Voici le graphique de " + player.getDiscordUser().getName() + " :").complete();
    event.getTextChannel().sendFile(chartPicture, player.getDiscordUser().getName() + "Graph.png").complete();
    
  }

  private XYChart createChart(List<DatedFullTier> datedFullTier, String playerName) throws NoValueRankException {

    List<Number> valueData = new ArrayList<>();

    for(int i = 0; i < datedFullTier.size(); i++) {
      try {
        valueData.add(datedFullTier.get(i).getFullTier().value());
      } catch (NoValueRankException e) {
        valueData.add(null);
      }
    }
    
    boolean hasANumber = false;
    
    for(Number number : valueData) {
      if(number != null) {
        hasANumber = true;
      }
    }
    
    if(!hasANumber) {
      throw new NoValueRankException(playerName + " n'a pas encore été classé !");
    }
    
    List<Date> dateData = new ArrayList<>();

    for(int i = 0; i < datedFullTier.size(); i++) {
      dateData.add(datedFullTier.get(i).getCreationTime().toDate());
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
    
    chart.setYAxisLabelOverrideMap(Ressources.getTableCorrespondanceRank());
    chart.getStyler().setDatePattern(DATE_PATTERN);
    
    chart.getStyler().setAntiAlias(true);
    
    return chart;
  }


}
