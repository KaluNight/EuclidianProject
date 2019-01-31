package ch.euclidian.main.model.discord.command.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.euclidian.main.util.Ressources;

public class LeaveCommand extends Command {

  public LeaveCommand() {
    this.name = "leave";
    this.help = "Supprimer la liste de musique et fait quitter le bot du channel";
    this.guildOnly = true;
  }

  @Override
  protected void execute(CommandEvent event) {
    event.getTextChannel().sendTyping().complete();
    Ressources.getMusicBot().leaveVoiceChannel();
    event.getChannel().sendMessage("J'ai quitté le channel et remis à zéro ma liste").queue();
  }

}
