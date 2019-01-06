package util;

import java.util.List;

import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.endpoints.static_data.dto.Champion;

public class Ressources {

  private static RiotApi riotApi;
  
  private static List<Champion> champions;
  
  
  private Ressources() {
  }
  
  public static RiotApi getRiotApi() {
    return riotApi;
  }

  public static void setRiotApi(RiotApi riotApi) {
    Ressources.riotApi = riotApi;
  }

  public static List<Champion> getChampions() {
    return champions;
  }

  public static void setChampions(List<Champion> championList) {
    Ressources.champions = championList;
  }
  
}
