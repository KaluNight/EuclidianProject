package util;

import java.util.ArrayList;
import java.util.List;

import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.static_data.dto.Champion;
import net.rithms.riot.constant.Platform;

public class Ressources {

  private static RiotApi riotApi;

  private static List<Champion> championsData = new ArrayList<>();

  private Ressources() {
  }

  /**
   * Return the champion data by the id, if the champion is not in the list, get from the api and cache it.
   * @param id of the Champion
   * @return Champion object
   * @throws RiotApiException 
   */
  public synchronized static Champion getChampionDataById(int id) throws RiotApiException {
    for(int i = 0; i < championsData.size(); i++) {
      if(championsData.get(i).getId() == id) {
        return championsData.get(i);
      }
    }
    Champion championToReturn = Ressources.getRiotApi().getDataChampion(Platform.EUW, id);
    championsData.add(championToReturn);
    return championToReturn;
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

}
