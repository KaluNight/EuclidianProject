package ch.euclidian.main.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

public class BotMusicManager {

  private AudioPlayerManager playerManager;
  private MusicManager musicManager;
  private VoiceChannel actualVoiceChannel;
  private TextChannel actualTextChannel;
  private AudioManager audioManager;

  public BotMusicManager() {
    this.playerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(playerManager);
    AudioSourceManagers.registerLocalSource(playerManager);
    musicManager = new MusicManager(playerManager);
  }

  public void loadAndPlay(final TextChannel channel, final String trackUrl) {
    setActualTextChannel(channel);
    
    playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
      @Override
      public void trackLoaded(AudioTrack track) {
        channel.sendMessage("Ajout à la liste de " + track.getInfo().title).queue();

        play(musicManager, track);
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        AudioTrack firstTrack = playlist.getSelectedTrack();

        if(firstTrack != null) {
          channel.sendMessage("Ajout à la liste de : " + firstTrack.getInfo().title + ")").queue();
          play(musicManager, firstTrack);
        }else {
          for(AudioTrack track : playlist.getTracks()) {
            musicManager.scheduler.queue(track);
          }
          channel.sendMessage("Ajout de la playlist \"" + playlist.getName() + "\" dans la liste").queue();

          audioManager.openAudioConnection(actualVoiceChannel);
        }
      }

      @Override
      public void noMatches() {
        channel.sendMessage("L'url fournit n'est pas écoutable").queue();
      }

      @Override
      public void loadFailed(FriendlyException exception) {
        channel.sendMessage("Erreur dans le chargement de la musique : " + exception.getMessage()).queue();
      }
    });
  }
  
  public String skipActualTrack() {
    String oldTrack = "";
    
    oldTrack = musicManager.player.getPlayingTrack().getInfo().title;
    musicManager.scheduler.nextTrack();
    
    if(musicManager.player.getPlayingTrack() == null) {
      return "Musique \"" + oldTrack + "\" passé. Aucune musique suivante dans la liste";
    }else {
      String newTrack = musicManager.player.getPlayingTrack().getInfo().title;
      return "Musique \"" + oldTrack + "\" passé. Vous écoutez maintenant \"" + newTrack + "\"";
    }
  }
  
  public void leaveVoiceChannel() {
    musicManager.player.stopTrack();
    musicManager.scheduler.deleteTheQueue();
    audioManager.closeAudioConnection();
    setActualVoiceChannel(null);
    setActualTextChannel(null);
  }
  
  public void clearQueue() {
    musicManager.scheduler.deleteTheQueue();
  }

  private void play(MusicManager musicManager, AudioTrack track) {
    audioManager.openAudioConnection(actualVoiceChannel);

    musicManager.scheduler.queue(track);
  }

  public AudioPlayerManager getPlayerManager() {
    return playerManager;
  }
  public void setPlayerManager(AudioPlayerManager playerManager) {
    this.playerManager = playerManager;
  }
  public MusicManager getMusicManager() {
    return musicManager;
  }
  public void setMusicManager(MusicManager musicManager) {
    this.musicManager = musicManager;
  }

  public VoiceChannel getActualVoiceChannel() {
    return actualVoiceChannel;
  }

  public void setActualVoiceChannel(VoiceChannel actualVoiceChannel) {
    this.actualVoiceChannel = actualVoiceChannel;
  }

  public AudioManager getAudioManager() {
    return audioManager;
  }

  public void setAudioManager(AudioManager audioManager) {
    this.audioManager = audioManager;
  }

  public TextChannel getActualTextChannel() {
    return actualTextChannel;
  }

  public void setActualTextChannel(TextChannel actualTextChannel) {
    this.actualTextChannel = actualTextChannel;
  }

}
