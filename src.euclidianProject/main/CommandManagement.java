package main;

import java.awt.Color;
import java.util.ArrayList;

import model.Player;
import model.Postulation;
import model.Team;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;
import request.MessageBuilderRequest;

public class CommandManagement {

	//							Main Command Section
	//-------------------------------------------------------------------------

	public static String showCommand(String commande, User user) {
		return "Erreur dans le choix de l'affichage";
	}

	public static String registerCommand(String commande, User user) {
		if(commande.substring(0, 6).equalsIgnoreCase("player")) {
			return registerPlayerCommand(commande, user);
		}else {
			return "Erreur dans le choix de l'enregistrement. Note : Vous devez écrire \"register player VotrePseudo\" pour vous enregistrer";
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

	//							 Show Command
	//-----------------------------------------------------------------------

	public static ArrayList<MessageEmbed> showPostulationsCommand(String commande) throws RiotApiException {
		ArrayList<MessageEmbed> listesPostulation = new ArrayList<MessageEmbed>();
		for(int i = 0; i < Main.getPostulationsList().size(); i++) {
			listesPostulation.add(MessageBuilderRequest.createShowPostulation(Main.getPostulationsList().get(i), i + 1));
		}
		return listesPostulation;
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
			summoner = Main.getRiotApi().getSummonerByName(Platform.getPlatformByName(region), summonerName);
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
		category.createTextChannel("liste-de-pick").queue();
		category.createTextChannel("annonce-absence").queue();
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
		String lolPseudo = "";
		try {
			lolPseudo = postulation[1].split(":")[1].replaceAll(" ", "");
		} catch (Exception e) {
			return "Erreur dans le format du Pseudo. (Format : \"Mon pseudo : *Pseudo*\")";
		}

		Summoner summoner;
		try {
			summoner = Main.getRiotApi().getSummonerByName(Platform.EUW, lolPseudo);
		} catch (RiotApiException e) {
			return "L'api de Riot n'est actuellement pas disponible. Nous ne pouvons pas valider votre pseudo, merci de réssayer plus tard.";
		} catch (IllegalArgumentException e) {
			return "Votre pseudo n'est pas valide. Merci de vérifier la typographie du pseudo (Note : Il doit obligatoirement être de la région EUW)";
		}

		String[] position;

		try {
			position = postulation[2].split(":")[1].split(",");
		}catch (Exception e) {
			return "Erreur dans le format des rôles. (Format : \"Les rôles que je peux jouer : *Role, Role, Role*\")";
		}

		ArrayList<Role> roles = new ArrayList<Role>();

		try {
			for(int i = 0; i < position.length; i++) {
				position[i] = position[i].replaceAll(" ", "");
				if(Main.getPositionRoleByName(position[i]) == null) {
					throw new NullPointerException();
				}else {
					roles.add(Main.getPositionRoleByName(position[i]));
				}
			}
		}catch (NullPointerException e) {
			return "Erreur dans la sélection des postes !";
		}

		String horaire = "";
		try {
			horaire = postulation[3].split(":")[1];
		}catch (Exception e) {
			return "Erreur dans le format de l'heure. (Format : \"Horaires : *VosHoraires*\")";
		}

		int index = Main.getPostulationIndexByMember(member);

		Postulation postulationObject = new Postulation(member, summoner, roles, horaire);

		ArrayList<Role> roleWithPostulant = new ArrayList<Role>();
		roleWithPostulant.addAll(roles);
		roleWithPostulant.add(Main.getPostulantRole());

		if(index > -1) {
			Main.getPostulationsList().remove(index);
			Main.getPostulationsList().add(postulationObject);
			Main.getController().modifyMemberRoles(member, roleWithPostulant).queue();
			return "Votre postulation a bien été modifié";
		}else {
			Main.getPostulationsList().add(postulationObject);
			Main.getController().addRolesToMember(member, roleWithPostulant).queue();
			return "Merci d'avoir postulé ! Vous recevrez des informations concernant votre potentiel recrutement très bientôt !\n"
			+ "Votre postulations (Vous pouvez la modifier en renvoyant une postulation) : \n \n"
			+ postulationObject.toString();
		}
	}


}
