package ch.euclidian.main.refresh.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.euclidian.main.Main;
import ch.euclidian.main.model.InfoCard;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.model.Team;
import ch.euclidian.main.util.NameConversion;
import ch.euclidian.main.util.Ressources;
import ch.euclidian.main.util.request.MessageBuilderRequest;
import ch.euclidian.main.util.request.RiotRequest;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.constant.Platform;

public class ContinuousPanelRefresh implements Runnable {

  private static final String ID_PANNEAU_DE_CONTROLE = "517436744124334091";

  private static boolean running;

  private static LocalDateTime nextRefreshPanel;

  private static HashMap<String, CurrentGameInfo> currentGames = new HashMap<>();

  private static List<Long> gamesIdAlreadySended = new ArrayList<>();

  private static List<InfoCard> infoCards = new ArrayList<>();

  private static Message messagePanel;

  private static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

  private static Logger logger = LoggerFactory.getLogger(ContinuousPanelRefresh.class);

  @Override
  public void run() {
    try {
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

    } catch (Exception e) {
      logger.error(e.getMessage());
    }finally {
      setRunning(false);
    }
  }

  private void manageInfoCards() {
    TextChannel controlPannel = Main.getGuild().getTextChannelById(ID_PANNEAU_DE_CONTROLE);

    List<InfoCard> messageSended = createInfoCards(controlPannel);

    infoCards.addAll(messageSended);

    deleteOlderInfoCards();
  }

  private void deleteOlderInfoCards() {
    List<InfoCard> cardsToRemove = new ArrayList<>();

    for(int i = 0; i < infoCards.size(); i++) {
      InfoCard card = infoCards.get(i);

      if(card.getCreationTime().plusMinutes(30).isBeforeNow()) {
        cardsToRemove.add(card);
      }
    }

    for(int i = 0; i < cardsToRemove.size(); i++) {
      try {      
        cardsToRemove.get(i).getMessage().delete().complete();
        cardsToRemove.get(i).getTitle().delete().complete();
        infoCards.remove(cardsToRemove.get(i));
      }catch (Exception e) {
        logger.warn("Impossible de delete message : {}", e.getMessage(), e);
      }
    }
  }

  private List<InfoCard> createInfoCards(TextChannel controlPannel){

    ArrayList<InfoCard> cards = new ArrayList<>();
    ArrayList<Player> playersAlreadyGenerated = new ArrayList<>();

    for(int i = 0; i < Main.getPlayerList().size(); i++) {
      Player player = Main.getPlayerList().get(i);

      if(!playersAlreadyGenerated.contains(player)) {

        CurrentGameInfo currentGameInfo = currentGames.get(player.getSummoner().getId());

        if(currentGameInfo != null && !gamesIdAlreadySended.contains(currentGameInfo.getGameId())) {
          List<Player> listOfPlayerInTheGame = checkIfOthersPlayersIsKnowInTheMatch(currentGameInfo);
          InfoCard card = null;

          if(listOfPlayerInTheGame.size() == 1) {
            MessageEmbed messageCard = MessageBuilderRequest.createInfoCard1summoner(
                player.getDiscordUser(), player.getSummoner(), currentGameInfo);
            if(messageCard != null) {
              card = new InfoCard(listOfPlayerInTheGame, messageCard);
            }
          }else if(listOfPlayerInTheGame.size() > 1) {
            MessageEmbed messageCard = MessageBuilderRequest.createInfoCardsMultipleSummoner(listOfPlayerInTheGame, currentGameInfo);

            if(messageCard != null) {
              card = new InfoCard(listOfPlayerInTheGame, messageCard);
            }
          }

          playersAlreadyGenerated.addAll(listOfPlayerInTheGame);
          gamesIdAlreadySended.add(currentGameInfo.getGameId());

          if(card != null) {
            List<Player> players = card.getPlayers();

            StringBuilder title = new StringBuilder();
            title.append("Info sur la partie de");

            List<String> playersName = NameConversion.getListNameOfPlayers(players);

            for(int j = 0; j < card.getPlayers().size(); j++) {
              if(playersName.size() == 1) {
                title.append(" " + playersName.get(j));
              } else if(j + 1 == playersName.size()) {
                title.append(" et de " + playersName.get(j));
              } else if(j + 2 == playersName.size()) {
                title.append(" " + playersName.get(j));
              }else {
                title.append(" " + playersName.get(j) + ",");
              }
            }

            card.setTitle(controlPannel.sendMessage(title.toString()).complete());
            card.setMessage(controlPannel.sendMessage(card.getCard()).complete());

            cards.add(card);
          }
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

    for(int i = 0; i < Main.getPlayerList().size(); i++) {
      CurrentGameInfo actualGame = null;

      try {
        actualGame = Ressources.getRiotApi().getActiveGameBySummoner(Platform.EUW, Main.getPlayerList().get(i).getSummoner().getId());
      } catch (RiotApiException e) {
        logger.info(e.getMessage());
      }
      currentGames.put(Main.getPlayerList().get(i).getSummoner().getId(), actualGame); //Can be null
    }

    stringMessage.append("__**Panneau de contrôle**__\n \n");

    for(int i = 0; i < teamList.size(); i++) {

      stringMessage.append("**Division " + teamList.get(i).getName() + "**\n \n");

      ArrayList<Player> playersList = teamList.get(i).getPlayers();

      for(int j = 0; j < playersList.size(); j++) {
        stringMessage.append(playersList.get(j).getSummoner().getName() + " (" + playersList.get(j).getDiscordUser().getAsMention() + ") : ");

        CurrentGameInfo actualGame = currentGames.get(playersList.get(j).getSummoner().getId());

        if(actualGame == null) {
          stringMessage.append("Pas en game\n");
        }else {
          stringMessage.append(RiotRequest.getActualGameStatus(actualGame) + "\n");
        }

      }
      stringMessage.append(" \n");
    }

    stringMessage.append("\nDernier rafraichissement à " + DateTime.now().plusHours(1).toString(timeFormatter) 
        + " | *Rafraichi toutes les 3 minutes*");

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

  public static List<InfoCard> getInfoCards() {
    return infoCards;
  }

  public static void setInfoCards(List<InfoCard> infoCards) {
    ContinuousPanelRefresh.infoCards = infoCards;
  }

}
