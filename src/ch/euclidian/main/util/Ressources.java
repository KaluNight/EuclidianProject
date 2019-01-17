package ch.euclidian.main.util;

import java.util.ArrayList;
import java.util.List;

import ch.euclidian.main.model.Champion;
import me.philippheuer.twitch4j.TwitchClient;
import me.philippheuer.twitch4j.message.MessageInterface;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;

public class Ressources {
  
  private static String twitchClientId;
  private static String twitchClientSecret;
  private static String twitchCredential;
  
  private static TwitchClient twitchApi;
  
  private static MessageInterface messageInterface;

  private static RiotApi riotApi;

  private static List<Champion> championsData = new ArrayList<>();

  private Ressources() {
  }

  /**
   * Return the champion data by the id, if the champion is not in the list, return null
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

  public static void resetChampionCache() {
    championsData = new ArrayList<>();
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
}
