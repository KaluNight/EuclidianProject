package main;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TimerTask;

import model.Player;
import model.Team;
import net.dv8tion.jda.core.entities.Message;
import net.rithms.riot.api.RiotApiException;
import request.RiotRequest;

public class ContinuousDataChecking extends TimerTask{

	private static final long MINUTES_BETWEN_EACH_REFRESH = 2;
	
	private static final String ID_PANNEAU_DE_CONTROLE = "517436744124334091";
	
	private static LocalDateTime nextRefreshPanel;
	
	private static Message messagePanel;
	
	@Override
	public void run() {
		
		if(messagePanel == null) {
			setMessagePanel(Main.getGuild().getTextChannelById(ID_PANNEAU_DE_CONTROLE).sendMessage("__**Panneau de controle**__\n \n*En chargement*").complete());
		}
		
		if(nextRefreshPanel == null) {
			nextRefreshPanel = LocalDateTime.now();
		}
		
		if(LocalDateTime.now().isAfter(nextRefreshPanel)) {
			
			try {
				refreshPannel();
			} catch (RiotApiException e) {
				System.out.println("Api Max Calls");
				e.printStackTrace();
			}
			
			setNextRefreshPanel(LocalDateTime.now().plusMinutes(MINUTES_BETWEN_EACH_REFRESH));
		}
		
	}
	
	private void refreshPannel() throws RiotApiException {
		
		ArrayList<Team> teamList = Main.getTeamList();
		
		StringBuilder stringMessage = new StringBuilder();
		
		stringMessage.append("__**Panneau de controle**__\n \n");
		
		for(int i = 0; i < teamList.size(); i++) {
			
			stringMessage.append("**Division " + teamList.get(i).getName() + "**\n \n");
			
			ArrayList<Player> playersList = teamList.get(i).getPlayers();
			
			for(int j = 0; j < teamList.size(); j++) {
				stringMessage.append(playersList.get(j).getSummoner().getName() + "(" + playersList.get(j).getDiscordUser().getName() + ") : ");
				
				stringMessage.append(RiotRequest.getActualGameStatus(playersList.get(j).getSummoner()) + "\n");
			}
			
			stringMessage.append(" \n");
		}
		
	}

	public static LocalDateTime getNextRefreshPanel() {
		return nextRefreshPanel;
	}

	public static void setNextRefreshPanel(LocalDateTime lastRefreshPanel) {
		ContinuousDataChecking.nextRefreshPanel = lastRefreshPanel;
	}

	public static Message getMessagePanel() {
		return messagePanel;
	}

	public static void setMessagePanel(Message message) {
		ContinuousDataChecking.messagePanel = message;
	}
	
}
