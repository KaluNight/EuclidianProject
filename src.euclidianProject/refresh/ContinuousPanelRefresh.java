package refresh;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.Main;
import model.InfoCard;
import model.Player;
import model.Team;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.constant.Platform;
import request.MessageBuilderRequest;
import request.RiotRequest;
import util.Ressources;

public class ContinuousPanelRefresh extends Thread{

  private static final String ID_PANNEAU_DE_CONTROLE = "517436744124334091";

  private static boolean running;

  private static LocalDateTime nextRefreshPanel;

  private static HashMap<Long, CurrentGameInfo> currentGames = new HashMap<>();
  
  private static List<Long> gamesIdAlreadySended = new ArrayList<>();

  private static List<InfoCard> infoCards = new ArrayList<>();

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

    List<InfoCard> messageToSend = createInfoCards();
    
    for(int i = 0; i < messageToSend.size(); i++) {
      InfoCard card = messageToSend.get(i);
      List<Player> players = messageToSend.get(i).getPlayers();
      
      StringBuilder title = new StringBuilder();
      title.append("Info sur la partie de");

      for(int j = 0; j < card.getPlayers().size(); j++) {
        if(j + 1 == players.size()) {
          title.append(" et de " + players.get(j).getDiscordUser().getAsMention());
        }else if(j + 2 == players.size()) {
          title.append(" " + players.get(j).getDiscordUser().getAsMention());
        }else {
          title.append(" " + players.get(j).getDiscordUser().getAsMention() + ",");
        }
      }
      
      controlPannel.sendMessage(title.toString()).complete();
      Message cardMessage = controlPannel.sendMessage(infoCards.get(i).getCard()).complete();
      messageToSend.get(i).setMessage(cardMessage);
    }
    
    infoCards.addAll(messageToSend);
    
    deleteOlderInfoCards();
  }
  
  private void deleteOlderInfoCards() {
    List<InfoCard> cardsToRemove = new ArrayList<>();
    
    for(int i = 0; i < infoCards.size(); i++) {
      InfoCard card = infoCards.get(i);
      
      if(card.getCreationTime().plusHours(1).isAfterNow()) {
        cardsToRemove.add(card);
      }
    }
    
    for(int i = 0; i < cardsToRemove.size(); i++) {
      infoCards.remove(cardsToRemove.get(i));
      cardsToRemove.get(i).getMessage().delete().complete();
    }
  }
  
  private List<InfoCard> createInfoCards(){

    ArrayList<InfoCard> cards = new ArrayList<>();
    ArrayList<Player> playersAlreadyGenerated = new ArrayList<>();

    for(int i = 0; i < Main.getPlayerList().size(); i++) {
      Player player = Main.getPlayerList().get(i);

      if(!playersAlreadyGenerated.contains(player)) {

        CurrentGameInfo currentGameInfo = currentGames.get(player.getSummoner().getId());
        
        if(currentGameInfo != null && !gamesIdAlreadySended.contains(currentGameInfo.getGameId())) {
          List<Player> listOfPlayerInTheGame = checkIfOthersPlayersIsKnowInTheMatch(currentGameInfo);

          if(listOfPlayerInTheGame.size() == 1) {
            MessageEmbed messageCard = MessageBuilderRequest.createInfoCard1summoner(
                player.getDiscordUser(), player.getSummoner(), currentGameInfo);
            if(messageCard != null) {
              InfoCard card = new InfoCard(listOfPlayerInTheGame, messageCard);
              cards.add(card);
            }
          }else if(listOfPlayerInTheGame.size() > 1) {
            MessageEmbed messageCard = MessageBuilderRequest.createInfoCardsMultipleSummoner(listOfPlayerInTheGame, currentGameInfo);
            
            if(messageCard != null) {
              InfoCard card = new InfoCard(listOfPlayerInTheGame, messageCard);
              cards.add(card);
            }
          }
          playersAlreadyGenerated.addAll(listOfPlayerInTheGame);
          gamesIdAlreadySended.add(currentGameInfo.getGameId());
        }
      }
    }
    return cards;
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
