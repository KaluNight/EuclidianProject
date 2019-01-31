package ch.euclidian.main.model.discord.command.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.euclidian.main.util.Ressources;

public class ResetCommand extends Command {

  public ResetCommand() {
    this.name = "reset";
    this.help = "Supprime toutes les musiques de la liste sans skip la musique actuelle";
    this.guildOnly = true;
  }
  
  @Override
  protected void execute(CommandEvent event) {
    event.getTextChannel().sendTyping().complete();
    Ressources.getMusicBot().clearQueue();
    event.getTextChannel().sendMessage("J'ai supprimé les musiques qui était dans ma liste d'attente").queue();
  }

}
