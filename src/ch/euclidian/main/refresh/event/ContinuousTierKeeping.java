package ch.euclidian.main.refresh.event;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import ch.euclidian.main.Main;
import ch.euclidian.main.model.DatedFullTier;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.util.LogHelper;
import ch.euclidian.main.util.request.RiotRequest;

public class ContinuousTierKeeping implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(ContinuousTierKeeping.class);

  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private static final String FOLDER_TO_TIER_SAVE = "ressources/tierData/";

  private HashMap<String, DatedFullTier> actualTiers = new HashMap<>();

  private static boolean running = false;

  @Override
  public void run() {
    setRunning(true);
    try {
      for(Player player : Main.getPlayerList()) {
        actualTiers.put(player.getDiscordUser().getId(), new DatedFullTier(RiotRequest.getSoloqRank(player.getSummoner().getId())));
      }

      HashMap<String, List<DatedFullTier>> listsOfDatedFullTier = loadTierSave();

      addNewDataToSave(listsOfDatedFullTier);

      saveTiers(listsOfDatedFullTier);

    }catch(Exception e) {
      logger.error(e.toString());
    }finally {
      setRunning(false);
    }
  }

  private void saveTiers(HashMap<String, List<DatedFullTier>> listsOfDatedFullTier) throws IOException {
    for(Player player : Main.getPlayerList()) {
      List<DatedFullTier> dataPlayer = listsOfDatedFullTier.get(player.getDiscordUser().getId());

      try (Writer writer = new FileWriter(FOLDER_TO_TIER_SAVE + player.getDiscordUser().getId() + ".json");){
        gson.toJson(dataPlayer, writer);
      } catch(IOException e) {
        LogHelper.logSender("La sauvegarde des tier de " + player.getDiscordUser().getName() + " n'a pas pu être enregistré");
      }
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

  private HashMap<String, List<DatedFullTier>> loadTierSave() throws IOException {
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

  public static boolean isRunning() {
    return running;
  }

  private static void setRunning(boolean running) {
    ContinuousTierKeeping.running = running;
  }
}
