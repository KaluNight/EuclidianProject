package refresh;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import main.Main;
import model.Player;
import model.Team;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;
import request.RiotRequest;
import util.Ressources;

public class ContinuousPanelRefresh extends Thread{

  private static final String ID_PANNEAU_DE_CONTROLE = "517436744124334091";

  private static boolean running;

  private static LocalDateTime nextRefreshPanel;

  private static HashMap<Summoner, CurrentGameInfo> currentGames = new HashMap<>();

  private static HashMap<Long, Message> infoCards = new HashMap<>();

  private static Message messagePanel;

  private static Logger logger = LoggerFactory.getLogger(ContinuousPanelRefresh.class);

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

    List<MessageEmbed> messageToSend = createInfoCards();

  }

  private List<MessageEmbed> createInfoCards(){

    for(int i = 0; i < Main.getPlayerList().size(); i++) {
      CurrentGameInfo currentGameInfo = currentGames.get(Main.getPlayerList().get(i).getSummoner());

      if(currentGameInfo != null) {

        List<Player> listOfPlayerInTheGame = checkIfOthersPlayersIsKnowInTheMatch(currentGameInfo);

        if(listOfPlayerInTheGame.size() == 1) {
          
        }
      }
    }

  }

  private List<Player> checkIfOthersPlayersIsKnowInTheMatch(CurrentGameInfo currentGameInfo){

    ArrayList<Player> listOfPlayers = new ArrayList<>();

    for(int i = 0; i < Main.getPlayerList().size(); i++) {
      for(int j = 0; j < currentGameInfo.getParticipants().size(); j++) {
        if(currentGameInfo.getParticipants().get(j).getSummonerId() == Main.getPlayerList().get(i).getSummoner().getId()) {
          listOfPlayers.add(Main.getPlayerList().get(i));
        }
      }
    }
    return listOfPlayers;
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

        CurrentGameInfo actualGame = null;

        try {
          actualGame = Ressources.getRiotApi().getActiveGameBySummoner(Platform.EUW, playersList.get(j).getSummoner().getId());
        } catch (RiotApiException e) {
          logger.info(e.getMessage());
        }

        if(actualGame == null) {
          stringMessage.append("Pas en game\n");
        }else {
          stringMessage.append(RiotRequest.getActualGameStatus(actualGame) + "\n");
        }

        currentGames.put(playersList.get(j).getSummoner().getId(), actualGame); //Can be null
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
