package refresh;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import main.Main;
import model.Player;
import model.Team;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import request.RiotRequest;

public class ContinuousPanelRefresh extends Thread{

  private static final String ID_PANNEAU_DE_CONTROLE = "517436744124334091";

  private static boolean running;

  private static LocalDateTime nextRefreshPanel;

  private static Message messagePanel;

  @Override
  public void run() {

    setRunning(true);

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

    messagePanel.editMessage(refreshPannel()).queue();

    manageInfoCards();

    setRunning(false);
  }

  private void manageInfoCards() {
    TextChannel controlPannel = Main.getGuild().getTextChannelById(ID_PANNEAU_DE_CONTROLE);

    List<Message> messages = controlPannel.getIterableHistory().stream()
        .limit(1000)
        .filter(m-> m.getAuthor().equals(Main.getJda().getSelfUser()))
        .collect(Collectors.toList());

    ArrayList<Message> messageToDelete = new ArrayList<>();
    
    for(int i = 0; i < messages.size(); i++) {
      if(messages.get(i).getId() != messagePanel.getId()) {
        messageToDelete.add(messages.get(i));
      }
    }
    
    
  }
  
  private List<MessageEmbed> createInfoCards(){
    
    ArrayList<MessageEmbed> infoCards = new ArrayList<>();
    
    
    
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

  public static boolean isRunning() {
    return running;
  }

  public static void setRunning(boolean running) {
    ContinuousPanelRefresh.running = running;
  }

}
