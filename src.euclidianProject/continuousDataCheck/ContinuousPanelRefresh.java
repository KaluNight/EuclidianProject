package continuousDataCheck;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import main.Main;
import model.Player;
import model.Team;
import net.dv8tion.jda.core.entities.Message;
import request.RiotRequest;

public class ContinuousPanelRefresh extends TimerTask{

	private static final long MINUTES_BETWEN_EACH_REFRESH = 2;

	private static final String ID_PANNEAU_DE_CONTROLE = "517436744124334091";

	private static LocalDateTime nextRefreshPanel;

	private static Message messagePanel;

	@Override
	public void run() {
		
		if(messagePanel == null) {
			
			List<Message> messages = Main.getGuild().getTextChannelById(ID_PANNEAU_DE_CONTROLE).getIterableHistory().complete();
			
			for(int i = 0; i < messages.size(); i++) {
				if(messages.get(i).getAuthor().equals(Main.getJda().getSelfUser())) {
					setMessagePanel(messages.get(i));
				}
			}
			
			if(messagePanel == null) {
				setMessagePanel(Main.getGuild().getTextChannelById(ID_PANNEAU_DE_CONTROLE).sendMessage("__**Panneau de controle**__\n \n*En chargement*").complete());
			}
		}

		if(nextRefreshPanel == null) {
			setNextRefreshPanel(LocalDateTime.now());
		}

		if(LocalDateTime.now().isAfter(nextRefreshPanel)) {

			messagePanel.editMessage(refreshPannel()).queue();

			setNextRefreshPanel(LocalDateTime.now().plusMinutes(MINUTES_BETWEN_EACH_REFRESH));
		}

	}

	private String refreshPannel() {

		ArrayList<Team> teamList = Main.getTeamList();
		StringBuilder stringMessage = new StringBuilder();

		stringMessage.append("__**Panneau de contrôle**__\n \n");

		for(int i = 0; i < teamList.size(); i++) {

			stringMessage.append("**Division " + teamList.get(i).getName() + "**\n \n");

			ArrayList<Player> playersList = teamList.get(i).getPlayers();

			for(int j = 0; j < playersList.size(); j++) {
				stringMessage.append(playersList.get(j).getSummoner().getName() + " (" + playersList.get(j).getDiscordUser().getAsMention() + ") : ");

				stringMessage.append(RiotRequest.getActualGameStatus(playersList.get(j).getSummoner()) + "\n");
			}

			stringMessage.append(" \n");
		}

		return stringMessage.toString();
	}

	public static LocalDateTime getNextRefreshPanel() {
		return nextRefreshPanel;
	}

	public static void setNextRefreshPanel(LocalDateTime lastRefreshPanel) {
		ContinuousPanelRefresh.nextRefreshPanel = lastRefreshPanel;
	}

	public static Message getMessagePanel() {
		return messagePanel;
	}

	public static void setMessagePanel(Message message) {
		ContinuousPanelRefresh.messagePanel = message;
	}

}
