package ch.euclidian.main.model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.euclidian.main.Main;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.model.Team;
import ch.euclidian.main.util.LogHelper;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class AddPlayerToTeamCommand extends Command {

  public AddPlayerToTeamCommand() {
    this.name = "addPlayerToTeam";
    this.arguments = "(Pseudo Discord) (Nom Team)";
    this.help = "Ajoute un joueur à une équipe";
    this.ownerCommand = true;
  }
  
  @Override
  protected void execute(CommandEvent event) {
    
    Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(event.getArgs());
    
    String pseudoDiscord = "";
    String team = "";
    
    while(m.find()) {
      pseudoDiscord = m.group(1);    
        break;
    }
    
    while (m.find()) { 
      team = m.group(1);
      break;
    }
    
    User user = Main.getJda().getUsersByName(pseudoDiscord, true).get(0);
    Player player = Main.getPlayersByDiscordId(user.getId());

    if(player == null) {
      event.reply(pseudoDiscord + " n'est pas enregistrée en tant que joueur");
      return;
    }

    Team teamToUse = Main.getTeamByName(team);

    Member member = Main.getGuild().getMember(user);

    Main.getController().addRolesToMember(member, teamToUse.getRole()).queue();

    LogHelper.logSender(player.getName() + " à été ajouté à l'équipe " + team);

    event.reply("Le joueur a bien été ajouté à l'équipe");
    
  }

}
