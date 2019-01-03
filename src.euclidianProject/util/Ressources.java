package util;

import net.rithms.riot.api.RiotApi;

public class Ressources {

  private static RiotApi riotApi;
  
  
  
  
  private Ressources() {
  }
  
  public static RiotApi getRiotApi() {
    return riotApi;
  }

  public static void setRiotApi(RiotApi riotApi) {
    Ressources.riotApi = riotApi;
  }
  
}
