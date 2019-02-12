package ch.euclidian.main.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import ch.euclidian.main.Main;
import ch.euclidian.main.model.Champion;
import ch.euclidian.main.model.CustomEmote;
import ch.euclidian.main.model.DatedFullTier;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.music.BotMusicManager;
import me.philippheuer.twitch4j.TwitchClient;
import me.philippheuer.twitch4j.endpoints.ChannelEndpoint;
import me.philippheuer.twitch4j.endpoints.StreamEndpoint;
import me.philippheuer.twitch4j.message.MessageInterface;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;

public class Ressources {

  public static final String FOLDER_TO_TIER_SAVE = "ressources/tierData/";
  
  public static final String FOLDER_TO_EMOTES = "ressources/images";

  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  
  private static Map<Double, Object> tableCorrespondanceRank;

  private static final Logger logger = LoggerFactory.getLogger(Ressources.class);

  private static String twitchClientId;
  private static String twitchClientSecret;
  private static String twitchCredential;

  private static TwitchClient twitchApi;

  private static MessageInterface messageInterface;

  private static ChannelEndpoint channelEndpoint;

  private static StreamEndpoint streamEndpoint;

  private static BotMusicManager musicBot;

  public static final String TWITCH_CHANNEL_NAME = "batailloneuclidien";

  private static RiotApi riotApi;

  private static List<Champion> championsData = new ArrayList<>();
  
  private static List<CustomEmote> customEmote;

  private Ressources() {}

  /**
   * Return the champion data by the id, if the champion is not in the list, return null
   * 
   * @param id of the Champion
   * @return Champion object or null if id is incorrect
   * @throws RiotApiException
   */
  public static Champion getChampionDataById(int id) {
    for(int i = 0; i < championsData.size(); i++) {
      if(championsData.get(i).getKey() == id) {
        return championsData.get(i);
      }
    }
    return null;
  }

  public static HashMap<String, List<DatedFullTier>> loadTierSave() throws IOException {
    HashMap<String, List<DatedFullTier>> listsOfDatedFullTier = new HashMap<>();

    for(Player player : Main.getPlayerList()) {
      try(FileReader fr =
          new FileReader(FOLDER_TO_TIER_SAVE + player.getDiscordUser().getId() + ".json");) {

        List<DatedFullTier> tierData = gson.fromJson(fr, new TypeToken<List<DatedFullTier>>() {}.getType());

        listsOfDatedFullTier.put(player.getDiscordUser().getId(), tierData);
      } catch(JsonSyntaxException | JsonIOException e) {
        LogHelper.logSender("Le fichier de rank de " + player.getName() + " est corrompu !");
      } catch(FileNotFoundException e) {
        logger.info((player.getName() + " ne possède pas de sauvegarde de rank"));
      }
    }
    return listsOfDatedFullTier;
  }

  public static List<DatedFullTier> loadTierOnePlayer(String discordId) throws FileNotFoundException {
    FileReader fr = new FileReader(FOLDER_TO_TIER_SAVE + discordId + ".json");

    List<DatedFullTier> tierData = gson.fromJson(fr, new TypeToken<List<DatedFullTier>>() {}.getType());

    return tierData;
  }

  public static void saveTiers(HashMap<String, List<DatedFullTier>> listsOfDatedFullTier) throws IOException {
    for(Player player : Main.getPlayerList()) {
      List<DatedFullTier> dataPlayer = listsOfDatedFullTier.get(player.getDiscordUser().getId());

      try (Writer writer = new FileWriter(FOLDER_TO_TIER_SAVE + player.getDiscordUser().getId() + ".json");){
        gson.toJson(dataPlayer, writer);
      } catch(IOException e) {
        LogHelper.logSender("La sauvegarde des tier de " + player.getDiscordUser().getName() + " n'a pas pu être enregistré");
      }
    }
  }

  public static RiotApi getRiotApi() {
    return riotApi;
  }

  public static void setRiotApi(RiotApi riotApi) {
    Ressources.riotApi = riotApi;
  }

  public static List<Champion> getChampions() {
    return championsData;
  }

  public static void setChampions(List<Champion> championList) {
    Ressources.championsData = championList;
  }

  public static TwitchClient getTwitchApi() {
    return twitchApi;
  }

  public static void setTwitchApi(TwitchClient twitchApi) {
    Ressources.twitchApi = twitchApi;
  }

  public static String getTwitchClientId() {
    return twitchClientId;
  }

  public static String getTwitchClientSecret() {
    return twitchClientSecret;
  }

  public static String getTwitchCredential() {
    return twitchCredential;
  }

  public static void setTwitchClientId(String twitchClientId) {
    Ressources.twitchClientId = twitchClientId;
  }

  public static void setTwitchClientSecret(String twitchClientSecret) {
    Ressources.twitchClientSecret = twitchClientSecret;
  }

  public static void setTwitchCredential(String twitchCredential) {
    Ressources.twitchCredential = twitchCredential;
  }

  public static MessageInterface getMessageInterface() {
    return messageInterface;
  }

  public static void setMessageInterface(MessageInterface messageInterface) {
    Ressources.messageInterface = messageInterface;
  }

  public static ChannelEndpoint getChannelEndpoint() {
    return channelEndpoint;
  }

  public static void setChannelEndpoint(ChannelEndpoint channelEndpoint) {
    Ressources.channelEndpoint = channelEndpoint;
  }

  public static StreamEndpoint getStreamEndpoint() {
    return streamEndpoint;
  }

  public static void setStreamEndpoint(StreamEndpoint streamEndpoint) {
    Ressources.streamEndpoint = streamEndpoint;
  }

  public static BotMusicManager getMusicBot() {
    return musicBot;
  }

  public static void setMusicBot(BotMusicManager musicBot) {
    Ressources.musicBot = musicBot;
  }

  public static Map<Double, Object> getTableCorrespondanceRank() {
    return tableCorrespondanceRank;
  }

  public static void setTableCorrespondanceRank(Map<Double, Object> tableCorrespondanceRank) {
    Ressources.tableCorrespondanceRank = tableCorrespondanceRank;
  }

  public static List<CustomEmote> getCustomEmote() {
    return customEmote;
  }

  public static void setCustomEmote(List<CustomEmote> customEmote) {
    Ressources.customEmote = customEmote;
  }
}
