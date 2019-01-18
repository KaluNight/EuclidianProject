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
  private AudioManager audioManager;

  public BotMusicManager() {
    this.playerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(playerManager);
    AudioSourceManagers.registerLocalSource(playerManager);
    musicManager = new MusicManager(playerManager);
  }

  public void loadAndPlay(final TextChannel channel, final String trackUrl) {

    playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
      @Override
      public void trackLoaded(AudioTrack track) {
        channel.sendMessage("Ajout à la liste de " + track.getInfo().title).queue();

        play(musicManager, track);
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        AudioTrack firstTrack = playlist.getSelectedTrack();

        if (firstTrack == null) {
          firstTrack = playlist.getTracks().get(0);
        }

        channel.sendMessage("Ajout à la liste de la playlist : " + firstTrack.getInfo().title + " (Première musique " + playlist.getName() + ")").queue();

        play(musicManager, firstTrack);
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

}
