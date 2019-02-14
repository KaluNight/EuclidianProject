package ch.euclidian.main.model.discord.command;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.euclidian.main.EventListener;
import ch.euclidian.main.Main;
import ch.euclidian.main.music.MusicManager;
import ch.euclidian.main.refresh.event.ContinuousPanelRefresh;
import ch.euclidian.main.refresh.event.ContinuousTimeChecking;
import ch.euclidian.main.util.Ressources;

public class ShutDownCommand extends Command {

  private static final Logger logger = LoggerFactory.getLogger(ShutDownCommand.class);

  public ShutDownCommand() {
    this.name = "stop";
    this.help = "Stop le bot";
    this.hidden = true;
    this.ownerCommand = true;
  }

  @Override
  protected void execute(CommandEvent event) {
    EventListener.getStatusReportMessage().editMessage("Status : Hors Ligne").complete();
    event.getTextChannel().sendTyping().complete();
    EventListener.getTimerTask().cancel();

    try {
      ContinuousTimeChecking.shutdownThreadPool(event.getTextChannel());
    } catch(InterruptedException e) {
      logger.warn("Les tâches n'ont pas put être arrêté normalement : {}", e.getMessage());
      System.exit(1);
      Thread.currentThread().interrupt();
    }

    for(int i = 0; i < ContinuousPanelRefresh.getInfoCards().size(); i++) {
      ContinuousPanelRefresh.getInfoCards().get(i).getMessage().delete().complete();
      ContinuousPanelRefresh.getInfoCards().get(i).getTitle().delete().complete();
    }

    MusicManager musicManager = Ressources.getMusicBot().getMusicManager();
    musicManager.player.stopTrack();
    musicManager.scheduler.deleteTheQueue();
    Ressources.getMusicBot().getAudioManager().closeAudioConnection();
    Ressources.getMusicBot().setActualVoiceChannel(null);
    
    event.getTextChannel().sendMessage("Je suis down !").complete();

    try {
      Main.saveDataTxt();
    } catch(IOException e) {
      logger.error("Erreur de sauvegarde : {}", e.getMessage());
    }

    try {
      Ressources.getMessageInterface().leaveChannel(Ressources.TWITCH_CHANNEL_NAME);
    } catch(NullPointerException e) {
      logger.warn("NullPointerException : {}", e.getMessage());
    }
    Main.getJda().shutdownNow();

    Ressources.getTwitchApi().disconnect();
    System.exit(0);
  }

}
