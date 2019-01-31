package ch.euclidian.main.model.twitch.command;

import ch.euclidian.main.util.LogHelper;
import ch.euclidian.main.util.Ressources;
import me.philippheuer.twitch4j.events.event.irc.ChannelMessageEvent;
import me.philippheuer.twitch4j.message.commands.Command;
import me.philippheuer.twitch4j.message.commands.CommandPermission;

public class LinkDiscordCommand extends Command {

  public LinkDiscordCommand() {
    setCommand("discord");
    setCategory("general");
    setDescription("Donne un lien permanant vers le discord");
    getRequiredPermissions().add(CommandPermission.EVERYONE);
  }

  @Override
  public void executeCommand(ChannelMessageEvent messageEvent) {
    super.executeCommand(messageEvent);

    Ressources.getMessageInterface().sendMessage(messageEvent.getChannel().getName(),
        "Le lien de notre Discord : https://discord.gg/BsxD9HD");
    LogHelper.logSender(messageEvent.getUser().getDisplayName() + " à demandé le discord de la team");
  }
}
