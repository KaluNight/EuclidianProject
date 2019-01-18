package ch.euclidian.main.refresh.event;

import ch.euclidian.main.Main;
import ch.euclidian.main.util.Ressources;
import ch.euclidian.main.util.request.MessageBuilderRequest;
import me.philippheuer.twitch4j.model.Channel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class ContinuousStreamOnlineChecking implements Runnable {

  private static boolean wasOnline = false;

  private static TextChannel annonceChannel = Main.getGuild().getTextChannelById("535729634973581312");

  private static Message embendedMessage;

  private static boolean running;

  @Override
  public void run() {
    setRunning(true);
    try {
      Channel channel = Ressources.getChannelEndpoint().getChannel(Ressources.TWITCH_CHANNEL_NAME);

      boolean streamIsLive = Ressources.getStreamEndpoint().isLive(channel);

      if(streamIsLive && !wasOnline) {
        setWasOnline(true);

        annonceChannel.sendMessage("Hey @here ! Nous sommes actuellement en live ! Hésitez pas à venir passez le bonjour !").queue();
        setEmbendedMessage(annonceChannel.sendMessage(MessageBuilderRequest.createInfoStreamMessage(channel)).complete());
      } else if (!streamIsLive && wasOnline) {
        setWasOnline(false);
        setEmbendedMessage(null);
      } else if(streamIsLive) {
        setEmbendedMessage(embendedMessage.editMessage(MessageBuilderRequest.createInfoStreamMessage(channel)).complete());
      }

    } finally {
      setRunning(false);
    }
  }

  public static void setAnnonceChannel(TextChannel annonceChannel) {
    ContinuousStreamOnlineChecking.annonceChannel = annonceChannel;
  }

  public static boolean isWasOnline() {
    return wasOnline;
  }

  private static void setWasOnline(boolean wasOnline) {
    ContinuousStreamOnlineChecking.wasOnline = wasOnline;
  }

  public static Message getEmbendedMessage() {
    return embendedMessage;
  }

  private static void setEmbendedMessage(Message embendedMessage) {
    ContinuousStreamOnlineChecking.embendedMessage = embendedMessage;
  }

  public static boolean isRunning() {
    return running;
  }

  public static void setRunning(boolean running) {
    ContinuousStreamOnlineChecking.running = running;
  }

}
