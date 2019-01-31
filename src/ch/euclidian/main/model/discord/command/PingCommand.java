package ch.euclidian.main.model.discord.command;

import java.time.temporal.ChronoUnit;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class PingCommand extends Command {

  public PingCommand() {
    this.name = "ping";
    this.help = "Envoie la latence du bot";
    this.guildOnly = false;
  }

  @Override
  protected void execute(CommandEvent event) {
    event.reply("Ping: ...", m -> {
      long ping = event.getMessage().getCreationTime().until(m.getCreationTime(), ChronoUnit.MILLIS);
      m.editMessage("Ping: " + ping + "ms | Websocket: " + event.getJDA().getPing() + "ms").queue();
    });
  }

}
