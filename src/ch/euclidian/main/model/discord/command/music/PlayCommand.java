package ch.euclidian.main.model.discord.command.music;

import java.util.List;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import ch.euclidian.main.Main;
import ch.euclidian.main.music.BotMusicManager;
import ch.euclidian.main.util.Ressources;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class PlayCommand extends Command {

  public PlayCommand() {
    this.name = "play";
    this.arguments = "LienDeLaMusique";
    this.help = "Ajoute dans la liste ou joue la musique envoyé";
    this.guildOnly = true;
  }

  @Override
  protected void execute(CommandEvent event) {

    event.getTextChannel().sendTyping().complete();
    String[] stringSplit = event.getMessage().getContentRaw().split(" ");
    if(stringSplit.length == 2) {
      String url = stringSplit[1];
      BotMusicManager botMusique = Ressources.getMusicBot();
      VoiceChannel actualVoiceChannel = botMusique.getActualVoiceChannel();

      if(actualVoiceChannel == null) {

        List<Channel> channels = Main.getGuild().getChannels();

        for(int i = 0; i < channels.size(); i++) {
          if(channels.get(i).getType().equals(ChannelType.VOICE)) {
            List<Member> inVoiceChannel = channels.get(i).getMembers();

            for(Member member : inVoiceChannel) {
              if(member.getUser().getId().equals(event.getAuthor().getId())) {
                actualVoiceChannel = Main.getGuild().getVoiceChannelById(channels.get(i).getId());
                break;
              }
            }
            if(actualVoiceChannel != null) {
              break;
            }
          }
        }
      }
      if(actualVoiceChannel == null) {
        event.getTextChannel().sendMessage("Veuillez rentrez dans un channel vocal pour que je puisse vous rejoindre").queue();
      } else {
        botMusique.setActualVoiceChannel(actualVoiceChannel);
        botMusique.loadAndPlay(event.getTextChannel(), url);
      }

    } else {
      event.getTextChannel()
          .sendMessage("Vous n'avez pas envoyé correctement l'URL avec le message," + " je ne peux pas faire grand chose sans ¯\\_(ツ)_/¯")
          .queue();
    }

  }

}
