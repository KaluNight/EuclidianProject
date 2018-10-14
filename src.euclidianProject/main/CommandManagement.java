package main;

import java.awt.Color;
import model.Team;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.restaction.RoleAction;

public class CommandManagement {
	
	public static String addCommand(String commande) {
		if(commande.substring(0, 4).equalsIgnoreCase("team")) {
			return addTeamCommand(commande.substring(5));
		}
		return "Erreur";
	}
	
	public static String addTeamCommand(String commande) {
		Main.getTeamList().add(new Team(commande));
		
		RoleAction role = Main.getController().createRole();
		role.setName("Division " + commande);
		role.setColor(Color.RED);
		role.setMentionable(true);
		role.setPermissions(Team.getPermissionsList());
		
		Role teamRole = role.complete();
		Role everyone = Main.getGuild().getPublicRole();
		
		Channel section = Main.getController().createCategory("Section " + commande).complete();
		section.createPermissionOverride(teamRole).setAllow(Team.getPermissionsList()).queue();
		section.createPermissionOverride(everyone).setDeny(Team.getPermissionsList()).queue();
		
		String sectionId = section.getId();
		
		Category category = Main.getGuild().getCategoryById(sectionId);
		
		category.createTextChannel("annonce-" + commande).queue();
		category.createTextChannel("general-" + commande).queue();
		category.createVoiceChannel("Général " + commande).queue();
		
		return "Equipe : " + commande + " créé !";
	}
}
