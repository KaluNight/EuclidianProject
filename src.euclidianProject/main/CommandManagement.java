package main;

import java.awt.Color;
import java.util.ArrayList;

import model.Player;
import model.Team;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class CommandManagement {

	
	//							Main Command Section
	//-------------------------------------------------------------------------
	
	public static String registerCommand(String commande, User user) {
		if(commande.substring(0, 6).equalsIgnoreCase("player")) {
			return registerPlayerCommand(commande, user);
		}else {
			return "Erreur dans l'enregistrement";
		}
	}
	
	public static String addCommand(String commande, User user) {
		if(commande.substring(0, 4).equalsIgnoreCase("team")) {
			return addTeamCommand(commande.substring(5));
		}else {
			return "Erreur dans le choix de l'ajout";
		}
	}
	
	public static String deleteCommand(String commande) {
		if(commande.substring(0, 4).equalsIgnoreCase("team")) {
			return deleteTeamCommand(commande);
		}else if (commande.substring(0, 6).equalsIgnoreCase("player")){
			return "Erreur";
		}else {
			return "Erreur dans le choix de la suppression";
		}
	}
	
	//						    Register Command
	//-----------------------------------------------------------------------
	
	private static String registerPlayerCommand(String commande, User user) {
		
		Member member = Main.getGuild().getMemberById(user.getId());
		
		for(int i = 0; i < member.getRoles().size(); i++) {
			if(member.getRoles().get(i).equals(Main.getRegisteredRole())) {
				return "Vous êtes déjà enregistée !";
			}
		}
		
		String[] info = commande.split(" ");
		
		String region = info[1];
		String summonerName = info[2];
		
		Summoner summoner;
		
		try {
			summoner = Main.getApi().getSummonerByName(Platform.getPlatformByName(region), summonerName);
		} catch (RiotApiException e) {
			e.printStackTrace();
			return "Erreur dans la région ou dans le nom d'invocateur ! Merci de réessayer";
		}
		
		Player player = new Player(user.getName(), user, summoner);
		
		Main.getPlayerList().add(player);
		
		Main.getController().addRolesToMember(member, Main.getRegisteredRole()).queue();
		
		return "Vous avez bien été enregisté !";
	}
	
	//							Add Command
	//-------------------------------------------------------------------------
	
	
	private static String addTeamCommand(String commande) {
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
		
		Main.getTeamList().add(new Team(commande, category, teamRole));
		
		return "Equipe : " + commande + " créé !";
	}
	
	
	//							Delete Command
	//-------------------------------------------------------------------------
	
	public static String deleteTeamCommand(String commande) {
		Team team = Main.getTeamByName(commande.split(" ")[1]);
		
		for(int i = 0; i < team.getCategory().getChannels().size(); i++) {
			team.getCategory().getChannels().get(i).delete().queue();
		}
		
		team.getCategory().delete().queue();
		team.getRole().delete().queue();
		
		String name = team.getName();
		
		Main.getTeamList().remove(team);
		
		return "Equipe " + name + " supprimé !";
	}
	
	//							Postulation
	//-------------------------------------------------------------------------
	
	public static String postulationCommand(String[] postulation, Member member) {
		String lolPseudo = postulation[1].split(":")[1].replaceAll(" ", "");
		
		String[] position = postulation[2].split(":")[1].split(",");
		
		
		
	}
	
	
}
