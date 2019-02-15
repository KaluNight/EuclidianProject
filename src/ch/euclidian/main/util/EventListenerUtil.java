package ch.euclidian.main.util;

import ch.euclidian.main.model.Champion;
import ch.euclidian.main.model.CustomEmote;
import ch.euclidian.main.model.Mastery;
import ch.euclidian.main.model.Tier;

public class EventListenerUtil {

  private EventListenerUtil() {
    //Hide default public constructor
  }
  
  public static void addToMasteryIfIsSame(CustomEmote emote) {
    for(Mastery mastery : Mastery.values()) {
      if(mastery.getName().equalsIgnoreCase(emote.getName())) {
        Ressources.getMasteryEmote().put(mastery, emote);
      }
    }
  }

  public static void addToTierIfisSame(CustomEmote emote) {
    for(Tier tier : Tier.values()) {
      if(tier.toString().replace(" ", "").equalsIgnoreCase(emote.getName())) {
        Ressources.getTierEmote().put(tier, emote);
      }
    }
  }

  public static void addToChampionIfIsSame(CustomEmote emote) {
    for(Champion champion : Ressources.getChampions()) {
      if(champion.getId().equals(emote.getName())) {
        champion.setEmote(emote.getEmote());
      }
    }
  }
}
