package ch.euclidian.main.refresh.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.euclidian.main.Main;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.util.LogHelper;
import ch.euclidian.main.util.Ressources;
import ch.euclidian.main.util.request.RiotRequest;
import me.philippheuer.twitch4j.events.EventSubscriber;
import me.philippheuer.twitch4j.events.event.channel.CheerEvent;
import me.philippheuer.twitch4j.events.event.channel.DonationEvent;
import me.philippheuer.twitch4j.events.event.channel.FollowEvent;
import me.philippheuer.twitch4j.events.event.channel.SubscriptionEvent;
import me.philippheuer.twitch4j.events.event.irc.ChannelMessageEvent;
import me.philippheuer.twitch4j.events.event.irc.UserBanEvent;
import me.philippheuer.twitch4j.events.event.irc.UserTimeoutEvent;
import net.rithms.riot.api.RiotApiException;

public class TwitchChannelEvent {

  private static Logger logger = LoggerFactory.getLogger(TwitchChannelEvent.class);

  @EventSubscriber
  public void onChannelMessage(ChannelMessageEvent event) {
    String message = event.getMessage();

    if(message.startsWith("!")) {
      String command = message.substring(1);

      if(command.startsWith("elo") && command.split(" ").length == 2) {

        Player player = Main.getPlayerBySummonerName(command.split(" ")[1]);

        String pseudo = command.split(" ")[1];

        if(player != null) {
          try {
            String returnMessage = pseudo + " est actuellement " + RiotRequest.getSoloqRank(player.getSummoner().getId());
            Ressources.getMessageInterface().sendMessage(event.getChannel().getName(), returnMessage);
            LogHelper.logSender("Requête SoloQ pour " + pseudo + " effectuer par : " + event.getUser().getName());
          } catch(RiotApiException e) {
            logger.info("Impossible to get SoloQ Rank : {}", e.getMessage());
          }
        }
      }


      else if(command.startsWith("discord")) {
        Ressources.getMessageInterface().sendMessage(event.getChannel().getName(), "Le lien de notre Discord : https://discord.gg/BsxD9HD");
        LogHelper.logSender(event.getUser().getName() + " à demandé le discord de la team");
      }
    }
  }

  @EventSubscriber
  public void onFollow(FollowEvent event) {
    String message = event.getUser().getName() + " vient de follow la chaîne !";
    Ressources.getMessageInterface().sendMessage(event.getChannel().getName(), message);

    LogHelper.logSender(event.getUser().getName() + " à follow la chaîne");
  }

  @EventSubscriber
  public void onSubscription(SubscriptionEvent event) {
    String message = "";

    // New Sub
    if (event.getMonths() <= 1) {
      message = event.getUser().getName() + " vient de s'abonner à la chaîne ! Merci <3";
      LogHelper.logSender(event.getUser().getName() + " c'est sub à la chaîne");
    }

    // Resub
    if (event.getMonths() > 1) {
      message = "Cela fait " + event.getMonths() + " que " + event.getUser().getName() + " est abonné à la chaîne ! Merci pour ton soutien <3";
      LogHelper.logSender(event.getUser().getName() + " c'est resub pour un total de " + event.getMonths());
    }

    if(!message.equals("")) {
      // Send Message
      Ressources.getMessageInterface().sendMessage(event.getChannel().getName(), message);
    }
  }

  @EventSubscriber
  public void onCheer(CheerEvent event) {
    String message = event.getUser().getName() + " a fait un don de " + event.getBits() + " Bits ! Merci à lui <3";
    Ressources.getMessageInterface().sendMessage(event.getChannel().getName(), message);

    LogHelper.logSender(event.getUser().getName() + "a fait un don de " + event.getBits() + " Bits");
  }

  @EventSubscriber
  public void onUserDonation(DonationEvent event) {
    String message = event.getUser().getName() + " a fait un don de "
        + event.getAmount() + " " + event.getCurrency().getDisplayName() + " ! Merci pour ton soutient <3";
    
    Ressources.getMessageInterface().sendMessage(event.getChannel().getName(), message);
    
    LogHelper.logSender(event.getUser().getName() + " a fait un don de "
        + event.getAmount() + " " + event.getCurrency().getDisplayName() + " venant de " + event.getSource());
  }
  
  @EventSubscriber
  public void onUserTimeout(UserTimeoutEvent event) {
    LogHelper.logSender(event.getUser() + "vient de se faire ban temporairement pendant " + event.getDuration() + " secondes");
  }

  @EventSubscriber
  public void onUserBan(UserBanEvent event) {
    LogHelper.logSender(event.getUser().getName() + " vient de se faire bannir. La raison est : " + event.getReason());
  }

}
