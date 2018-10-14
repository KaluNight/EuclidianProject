package main;

import java.util.ArrayList;
import java.util.List;

import model.Team;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter{

	private static final char PREFIX = '>';
	
	private static final String ADMIN_ROLE_ID = "497679745551695872";
	
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
				
				
				if (message.substring(0, 3).equalsIgnoreCase("Add")){
					String result = CommandManagement.addCommand(message.substring(4));
					event.getTextChannel().sendMessage(result).queue();
				}
				
				if (message.substring(0, 4).equals("stop")) {
					Main.getJda().getTextChannelsByName("log-bot", true).get(0).sendMessage("Je suis down !").complete();
					Main.getJda().shutdown();
				}
				
			}
		}
	}
}
