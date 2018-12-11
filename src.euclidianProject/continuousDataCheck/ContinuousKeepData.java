package continuousDataCheck;

import java.util.TimerTask;

import org.joda.time.DateTime;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.core.match.MatchHistory;
import com.merakianalytics.orianna.types.core.summoner.Summoner;

import main.Main;
import model.Player;

public class ContinuousKeepData extends TimerTask{

	private static int indexPlayerSelected = -1;
	
	private static DateTime weekDateStart; //TODO: Date need to be set Every week
	
	private static DateTime weekDateEnd; //TODO: Date need to be set Every week
	
	//TODO: Object for stack treated history of player
	
	@Override
	public void run() {
		
		if((indexPlayerSelected + 1) >= Main.getPlayerList().size()) {
			indexPlayerSelected = 0;
		}else {
			indexPlayerSelected++;
		}
		Player player = Main.getPlayerList().get(0);
		Summoner summoner = player.getSummoner();
		
		MatchHistory matchHistory = MatchHistory.forSummoner(summoner).withStartTime(weekDateStart).get();
		
		treatmentData(matchHistory); //TODO: return later an object with compiled data
	}
	
	private void treatmentData(MatchHistory matchHistory) {
		//TODO
	}
}
