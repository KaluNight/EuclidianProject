package main;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Team;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.RoleAction;

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

		if (message.length() == 0 || message.charAt(0) != PREFIX) {
			return;
		}

		message = message.substring(1);

		List<Role> list = event.getMember().getRoles();

		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).getId().equals(ADMIN_ROLE_ID)) {

				String command = message.split(" ")[0];

				if (command.equalsIgnoreCase("Add")){
					String result = CommandManagement.addCommand(message.substring(4), event.getAuthor());
					event.getTextChannel().sendMessage(result).queue();

				} else if (command.equalsIgnoreCase("delete")) {

					String result = CommandManagement.deleteCommand(message.substring(7));
					event.getTextChannel().sendMessage(result).queue();

				}else if (command.equalsIgnoreCase("register")) {

					String result = CommandManagement.registerCommand(message.substring(9), event.getAuthor());
					event.getTextChannel().sendMessage(result).queue();

				} else if (command.equals("stop")) {
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
		}
	}

	public static String getEnregistredPlayerRoleName() {
		return ENREGISTRED_PLAYER_ROLE_NAME;
	}
}
