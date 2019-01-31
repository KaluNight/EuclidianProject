package ch.euclidian.main.model.discord.command;

import java.util.List;
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
    this.arguments = "(Mention Discord) (Nom Team)";
    this.help = "Ajoute un joueur à une équipe";
    this.ownerCommand = true;
  }

  @Override
  protected void execute(CommandEvent event) {

    Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(event.getArgs());

    String team = "";

    while (m.find()) { 
      team = m.group(1);
    }

    List<Member> mentionned = event.getMessage().getMentionedMembers();
    User user;
    Member member;
    
    if(mentionned.isEmpty()) {
      event.reply("Vous n'avez mentionné aucun joueur !");
      return;
    }else {
      member = mentionned.get(0);
      user = member.getUser();
    }
    
    Player player = Main.getPlayersByDiscordId(user.getId());

    if(player == null) {
      event.reply(member.getNickname() + " n'est pas enregistrée en tant que joueur");
      return;
    }

    Team teamToUse = Main.getTeamByName(team);

    if(teamToUse == null) {
      event.reply("L'équipe sélectionner n'existe pas !");
      return;
    }
    
    teamToUse.getPlayers().add(player);
    Main.getController().addRolesToMember(member, teamToUse.getRole()).queue();

    LogHelper.logSender(player.getName() + " à été ajouté à l'équipe " + team);

    event.reply("Le joueur a bien été ajouté à l'équipe");

  }

}
