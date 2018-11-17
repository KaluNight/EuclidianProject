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
import net.dv8tion.jda.core.entities.PrivateChannel;
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

	public static String postulationCommand(String commande, User user) {
		if(commande.substring(0, 6).equalsIgnoreCase("accept")) {
			int index = Integer.parseInt(commande.split(" ")[1]) - 1;
			return postulationAcceptCommand(index, user);
		}else {
			return "Erreur dans le choix de l'action a faire";
		}
	}

	public static String registerCommand(String commande, User user) {
		if(commande.substring(0, 6).equalsIgnoreCase("player")) {
			return registerPlayerCommand(commande, user);
		}else {
			return "Erreur dans le choix de l'enregistrement. Note : Vous devez écrire \"register player VotreServeur VotrePseudo\" pour vous enregistrer";
		}
	}

	public static String addCommand(String commande, User user) {
		if(commande.substring(0, 4).equalsIgnoreCase("team")) {
			return addTeamCommand(commande.substring(5));
		}else if (commande.substring(0, 12).equalsIgnoreCase("playerToTeam")) {
			try {
			return addPlayerToTeam(commande.split(" ")[1], commande.split(" ")[2]);
			} catch (Exception e) {
				e.printStackTrace();
				return "Erreur dans les arguments de la commande (Note : >add playerToTeam DiscordPlayerName Team)";
			}
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
		logSender("Postulations affichées");
		
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
		
		String region;
		String summonerName;
		
		try {
		String[] info = commande.split(" ");

		region = info[1];
		summonerName = info[2];
		
		}catch(ArrayIndexOutOfBoundsException e) {
			return "Erreur dans l'enregistrement. Note : Vous devez écrire \"register player VotreServeur VotrePseudo\" pour vous enregistrer";
		}

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

		logSender(user.getName() + " c'est enregistré en tant que joueur");
		
		return "Vous avez bien été enregistré !";
	}

	//							Add Command
	//-------------------------------------------------------------------------


	private static String addPlayerToTeam(String discordName, String team) {
		User user = Main.getJda().getUsersByName(discordName, true).get(0);
		Player player = Main.getPlayersByDiscordId(user.getId());
		
		if(player == null) {
			return discordName + " n'est pas enregistrée en tant que joueur";
		}
		
		Team teamToUse = Main.getTeamByName(team);
		Team teamBase = teamToUse;
		
		teamToUse.getPlayers().add(player);
		
		Main.getPlayerList().add(player);
		
		Member member = Main.getGuild().getMember(user);
		
		Main.getController().addRolesToMember(member, teamToUse.getRole()).queue();
		
		Main.getTeamList().remove(teamBase);
		Main.getTeamList().add(teamToUse);
		
		logSender(player.getName() + " à été ajouté à l'équipe " + team);
		
		return "Le joueur a bien été ajouté à l'équipe";
	}
	
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
		
		ArrayList<Team> teamList = new ArrayList<Team>();
		teamList.addAll(Main.getTeamList());
		teamList.add(new Team(commande, category, teamRole));
		
		Main.setTeamList(teamList);
		
		logSender("Equipe " + commande + " créé");

		return "Equipe " + commande + " créé !";
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
		
		logSender("Equipe " + name + " supprimé");

		return "Equipe " + name + " supprimé !";
	}

	//							Postulation Command
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
			
			logSender("Postulation de " + member.getUser().getName() + " modifié");
			
			return "Votre postulation a bien été modifié";
		}else {
			Main.getPostulationsList().add(postulationObject);
			Main.getController().addRolesToMember(member, roleWithPostulant).queue();
			
			logSender("Nouvelle postulation créé par " + member.getUser().getName());
			
			return "Merci d'avoir postulé ! Vous recevrez des informations concernant votre potentiel recrutement très bientôt !\n"
			+ "Votre postulations (Vous pouvez la modifier en renvoyant une postulation) : \n \n"
			+ postulationObject.toString();
		}
	}

	private static String postulationAcceptCommand(int accepted, User user) {
		Postulation postulation;
		try {
			postulation = Main.getPostulationsList().get(accepted);
		}catch (IndexOutOfBoundsException e) {
			return "Erreur dans la sélection de la postulation (index)";
		}
		
		PrivateChannel privateChannel = postulation.getMember().getUser().openPrivateChannel().complete();
		privateChannel.sendTyping().queue();
		privateChannel.sendMessage("Votre postulation à été accepté, vous recevrez très bientôt des informations concernant votre futur affiliation, "
				+ "C'est " + user.getName() + " qui s'occupera de vous contacter.").queue();

		String result = "Vous avez accepter la postulation de " + postulation.getMember().getUser().getName() + ". "
				+ "Il a été automatiquement enregistré en tant que joueur.";

		Player player = new Player(postulation.getMember().getUser().getName(), postulation.getMember().getUser(), postulation.getSummoner());
		Main.getController().addRolesToMember(postulation.getMember(), Main.getRegisteredRole()).queue();
		
		Main.getPlayerList().add(player);
		
		Main.getPostulationsList().remove(accepted);
		
		logSender("Postulation de " + postulation.getMember().getUser().getName() + " accepté par " + user.getName());

		return result;
	}
	
	//								Log
	//----------------------------------------------------------------------
	
	private static void logSender(String str) {
		Main.getLogBot().sendMessage(str).queue();
	}

}
