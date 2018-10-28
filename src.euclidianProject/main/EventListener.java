package main;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import model.Team;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import net.rithms.riot.api.RiotApiException;

public class EventListener extends ListenerAdapter{

	private static final char PREFIX = '>';

	private static final String ADMIN_ROLE_ID = "497679745551695872";

	private static final String ENREGISTRED_PLAYER_ROLE_NAME = "Enregistré";

	@Override
	public void onReady(ReadyEvent event) {
		Main.getJda().getTextChannelsByName("log-bot", true).get(0).sendMessage("Je suis Up !").complete();
		Main.setGuild(Main.getJda().getTextChannelsByName("log-bot", true).get(0).getGuild());
		Main.setController(Main.getGuild().getController());

		ArrayList<Permission> teamMemberPermissionList = new ArrayList<Permission>();

		//Text Permission
		teamMemberPermissionList.add(Permission.MESSAGE_WRITE);
		teamMemberPermissionList.add(Permission.MESSAGE_READ);
		teamMemberPermissionList.add(Permission.MESSAGE_EMBED_LINKS);
		teamMemberPermissionList.add(Permission.MESSAGE_ATTACH_FILES);
		teamMemberPermissionList.add(Permission.MESSAGE_HISTORY);
		teamMemberPermissionList.add(Permission.MESSAGE_EXT_EMOJI);
		teamMemberPermissionList.add(Permission.MESSAGE_ADD_REACTION);

		//Voice permission
		teamMemberPermissionList.add(Permission.VOICE_CONNECT);
		teamMemberPermissionList.add(Permission.VOICE_USE_VAD);
		teamMemberPermissionList.add(Permission.VOICE_SPEAK);

		Team.setPermissionsList(teamMemberPermissionList);

		if(Main.getGuild().getRolesByName("Enregistré", true).isEmpty()) {
			try {
				RoleAction role = Main.getController().createRole();
				role.setName("Enregistré");
				role.setColor(Color.BLUE);
				role.setMentionable(false);
				role.setPermissions(Team.getPermissionsList());

				Role usableRole = role.complete();
				Main.setRegisteredRole(usableRole);
			} catch (Exception e) {
				System.err.println("Unknow Error");
			}
		} else {
			Main.setRegisteredRole(Main.getGuild().getRolesByName("Enregistré", true).get(0));
		}

		if(Main.getGuild().getRolesByName("Postulant", true).isEmpty()) {
			System.err.println("Please create Postulant role !");
		}else {
			Main.setPostulantRole(Main.getGuild().getRolesByName("Postulant", true).get(0));
		}

		ArrayList<Role> posteRole = new ArrayList<Role>();

		posteRole.add(Main.getGuild().getRolesByName("top", true).get(0));
		posteRole.add(Main.getGuild().getRolesByName("jungle", true).get(0));
		posteRole.add(Main.getGuild().getRolesByName("mid", true).get(0));
		posteRole.add(Main.getGuild().getRolesByName("adc", true).get(0));
		posteRole.add(Main.getGuild().getRolesByName("support", true).get(0));

		Main.setRolePosition(posteRole);

		try {
			Main.loadData();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContentRaw();

		List<Role> list = null;
		
		try {
		list = event.getMember().getRoles();
		} catch (NullPointerException e) {
			return;
		}
		
		boolean isAdmin = false;

		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).getId().equals(ADMIN_ROLE_ID)) {
				isAdmin = true;
			}
		}
		
		if(event.getTextChannel().getId().equalsIgnoreCase("497763778268495882") && message.charAt(0) != PREFIX && !isAdmin) {
			event.getMessage().delete().queue();
			PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
			privateChannel.sendTyping().queue();
			privateChannel.sendMessage("On envoie uniquement des demandes de Postulation sur ce channel ! "
					+ "(Note : Une postulation commence par \">postulation\")").queue();
		}
		
		if (message.length() == 0 || message.charAt(0) != PREFIX) {
			return;
		}

		message = message.substring(1);

		if(event.getTextChannel().getName().equals("postulation")) {
			String[] postulation = message.split("\n");
			if(postulation[0].equals("Postulation")) {
				event.getTextChannel().sendTyping().complete();
				String result = CommandManagement.postulationCommand(postulation, event.getMember());
				Message messageSend = event.getTextChannel().sendMessage(result).complete();

				PrivateChannel pc = event.getAuthor().openPrivateChannel().complete();

				pc.sendMessage("~Copie du Message~\n" + result).queue();

				messageSend.delete().queueAfter(10, TimeUnit.SECONDS);
				event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
			}else {
				if(!isAdmin) {
					event.getTextChannel().sendTyping().queue();
					Message messageResponse = event.getTextChannel().sendMessage("Les demandes de Postulation doivent commencer par \"Postulation\".").complete();
					event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
					messageResponse.delete().queueAfter(5, TimeUnit.SECONDS);
				}
			}
		}

		String command = message.split(" ")[0];

		if(isAdmin) {

			if (command.equalsIgnoreCase("Add")){
				event.getTextChannel().sendTyping().queue();
				String result = CommandManagement.addCommand(message.substring(4), event.getAuthor());
				event.getTextChannel().sendMessage(result).queue();

			} else if (command.equalsIgnoreCase("show")) {

				event.getTextChannel().sendTyping().queue();

				if(message.split(" ")[1].equalsIgnoreCase("postulations") || message.split(" ")[1].equalsIgnoreCase("postulation")) {
					try {
						ArrayList<MessageEmbed> listEmbended = CommandManagement.showPostulationsCommand(command);

						for(int i = 0; i < listEmbended.size(); i++) {
							event.getTextChannel().sendMessage(listEmbended.get(i)).queue();
						}
					} catch (RiotApiException e) {
						event.getTextChannel().sendMessage("L'api Riot est pas disponible").queue();
					}
				} else {
					String result = CommandManagement.showCommand(command, event.getAuthor());
					event.getTextChannel().sendMessage(result).queue();
				}

			}else if (command.equalsIgnoreCase("postulation")){

				event.getTextChannel().sendTyping().queue();
				String result = CommandManagement.postulationCommand(message.substring(12), event.getAuthor());
				event.getTextChannel().sendMessage(result).queue();
				
			} else if (command.equalsIgnoreCase("delete")) {

				event.getTextChannel().sendTyping().queue();
				String result = CommandManagement.deleteCommand(message.substring(7));
				event.getTextChannel().sendMessage(result).queue();

			} else if (command.equals("stop")) {
				event.getTextChannel().sendTyping().queue();
				Main.getJda().getTextChannelsByName("log-bot", true).get(0).sendMessage("Je suis down !").complete();
				try {
					Main.saveData();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Erreur Save");
				}
				Main.getJda().shutdown();
			}
		}

		if (command.equalsIgnoreCase("register")) {
			event.getTextChannel().sendTyping().queue();
			String result = CommandManagement.registerCommand(message.substring(9), event.getAuthor());
			event.getTextChannel().sendMessage(result).queue();
		}
	}

	public static String getEnregistredPlayerRoleName() {
		return ENREGISTRED_PLAYER_ROLE_NAME;
	}
}
