package ch.euclidian.main.model.discord.command;

import java.io.FileNotFoundException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.euclidian.main.Main;
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
      event.reply("Je n'ai pas encore récolté suffisament de données par rapport à ce joueur");
      return;
    }
  }

}
