package ch.euclidian.main.model.command.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.euclidian.main.util.Ressources;

public class SkipCommand extends Command {

  public SkipCommand() {
    this.name = "skip";
    this.help = "Passe la musique qui est actuellement joué";
    this.guildOnly = true;
  }

  @Override
  protected void execute(CommandEvent event) {
    event.getChannel().sendTyping().complete();
    event.reply(Ressources.getMusicBot().skipActualTrack());
  }

}
