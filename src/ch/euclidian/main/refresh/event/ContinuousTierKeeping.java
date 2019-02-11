package ch.euclidian.main.refresh.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.euclidian.main.Main;
import ch.euclidian.main.model.DatedFullTier;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.util.LogHelper;
import ch.euclidian.main.util.Ressources;
import ch.euclidian.main.util.request.RiotRequest;

public class ContinuousTierKeeping implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(ContinuousTierKeeping.class);

  private HashMap<String, DatedFullTier> actualTiers = new HashMap<>();

  private static boolean running = false;

  @Override
  public void run() {
    setRunning(true);
    try {
      for(Player player : Main.getPlayerList()) {
        actualTiers.put(player.getDiscordUser().getId(), new DatedFullTier(RiotRequest.getSoloqRank(player.getSummoner().getId())));
      }

      HashMap<String, List<DatedFullTier>> listsOfDatedFullTier = Ressources.loadTierSave();

      addNewDataToSave(listsOfDatedFullTier);

      Ressources.saveTiers(listsOfDatedFullTier);
      
      LogHelper.logSender("Tier des joueurs pour la journée sauvegardé");

    }catch(Exception e) {
      logger.error(e.toString());
    }finally {
      setRunning(false);
    }
  }

  private void addNewDataToSave(HashMap<String, List<DatedFullTier>> listsOfDatedFullTier) {
    for(Player player : Main.getPlayerList()) {
      DatedFullTier tier = actualTiers.get(player.getDiscordUser().getId());

      List<DatedFullTier> tierData = listsOfDatedFullTier.get(player.getDiscordUser().getId());

      if(tierData == null) {
        List<DatedFullTier> newSave = new ArrayList<>();
        newSave.add(tier);

        listsOfDatedFullTier.put(player.getDiscordUser().getId(), newSave);
      }else {
        tierData.add(tier);
      }
    }
  }

  public static boolean isRunning() {
    return running;
  }

  private static void setRunning(boolean running) {
    ContinuousTierKeeping.running = running;
  }
}
