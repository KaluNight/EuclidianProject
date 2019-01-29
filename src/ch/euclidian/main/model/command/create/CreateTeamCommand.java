package ch.euclidian.main.model.command.create;

import java.awt.Color;
import java.util.ArrayList;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.euclidian.main.Main;
import ch.euclidian.main.model.Team;
import ch.euclidian.main.util.LogHelper;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.restaction.RoleAction;

public class CreateTeamCommand extends Command{

  public CreateTeamCommand() {
    this.name = "createTeam";
    this.help = "Créer une équipe";
    this.arguments = "SigleDeLaTeam";
    this.ownerCommand = true;
  }
  
  @Override
  protected void execute(CommandEvent event) {
    RoleAction role = Main.getController().createRole();
    role.setName("Division " + event.getArgs());
    role.setColor(Color.RED);
    role.setMentionable(true);
    role.setPermissions(Team.getPermissionsList());

    Role teamRole = role.complete();
    Role everyone = Main.getGuild().getPublicRole();

    Channel section = Main.getController().createCategory("Section " + event.getArgs()).complete();
    section.createPermissionOverride(teamRole).setAllow(Team.getPermissionsList()).queue();
    section.createPermissionOverride(everyone).setDeny(Team.getPermissionsList()).queue();

    String sectionId = section.getId();

    net.dv8tion.jda.core.entities.Category category = Main.getGuild().getCategoryById(sectionId);

    category.createTextChannel("annonce-" + event.getArgs()).queue();
    category.createTextChannel("general-" + event.getArgs()).queue();
    category.createTextChannel("liste-de-pick").queue();
    category.createTextChannel("annonce-absence").queue();
    category.createVoiceChannel("Général " + event.getArgs()).queue();

    ArrayList<Team> teamList = new ArrayList<>();
    teamList.addAll(Main.getTeamList());
    teamList.add(new Team(event.getArgs(), category, teamRole));

    Main.setTeamList(teamList);

    LogHelper.logSender("Equipe " + event.getArgs() + " créé");

    event.reply("Equipe " + event.getArgs() + " créé !");
  }

}
