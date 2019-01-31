package ch.euclidian.main.model.twitch.command;

import java.util.List;
import ch.euclidian.main.Main;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.util.LogHelper;
import ch.euclidian.main.util.Ressources;
import ch.euclidian.main.util.request.RiotRequest;
import me.philippheuer.twitch4j.events.event.irc.ChannelMessageEvent;
import me.philippheuer.twitch4j.message.commands.Command;
import me.philippheuer.twitch4j.message.commands.CommandPermission;

public class TopEloCommand extends Command {

  public TopEloCommand() {
    // Command Configuration
    setCommand("topelo");
    setCommandAliases(new String[]{"bestelo", "topelos"});
    setCategory("general");
    setDescription("Montre les rangs de chaques membres d'équipe");
    getRequiredPermissions().add(CommandPermission.EVERYONE);
    setUsageExample("");
  }

  @Override
  public void executeCommand(ChannelMessageEvent messageEvent) {
    super.executeCommand(messageEvent);

    Ressources.getMessageInterface().sendMessage(messageEvent.getChannel().getName(), getRankOfAllTeamMembers());
    
    LogHelper.logSender(messageEvent.getUser().getDisplayName() + " à demandé l'elo des membres du bataillon");
  }

  private String getRankOfAllTeamMembers(){

    StringBuilder builder = new StringBuilder();

    for(int i = 0; i < Main.getTeamList().size(); i++) {
      List<Player> players = Main.getTeamList().get(i).getPlayers();
      for(int j = 0; j < players.size(); j++) {
        builder.append(players.get(j).getDiscordUser().getName() + " : " + RiotRequest.getSoloqRank(players.get(j).getSummoner().getId()));

        if(players.size() != i + 1 && Main.getTeamList().size() != i + 1) {
          builder.append(" | "); 
        }
      }
    }

    return builder.toString();
  }

}
