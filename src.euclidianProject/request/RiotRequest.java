package request;

import java.util.Iterator;
import java.util.Set;

import main.Main;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.league.dto.LeaguePosition;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class RiotRequest {

	public static String getSoloqRank(Platform platform, long summonerID) throws RiotApiException {
		Set<LeaguePosition> list = Main.getRiotApi().getLeaguePositionsBySummonerId(platform, summonerID);
		
		Iterator<LeaguePosition> it = list.iterator();
		
		String ligue = "Unranked";
		String rank = "";
		
		for(int i = 0; i < list.size(); i++) {
			LeaguePosition leaguePosition = it.next();
			
			if(leaguePosition.getQueueType().equals("RANKED_SOLO_5x5")) {
				ligue = leaguePosition.getTier();
				rank = leaguePosition.getRank();
			}
		}
		
		return ligue + " " + rank;
	}
	
	public static String getActualGameStatus(Summoner summoner) {
		
		CurrentGameInfo currentGameInfo = null;
		try {
			currentGameInfo = Main.getRiotApi().getActiveGameBySummoner(Platform.EUW, summoner.getId());
		}catch (RiotApiException e) {
			return "Pas en game";
		}
		
		String gameStatus = currentGameInfo.getGameMode();
		
		gameStatus += " (" + currentGameInfo.getGameType() + ") ";
		
		double minutesOfGames = currentGameInfo.getGameLength() / 60.0;
		String[] stringMinutesSecondes = Double.toString(minutesOfGames).split("\\.");
		int minutesGameLength = Integer.parseInt(stringMinutesSecondes[0]);
		int secondesGameLength = (int) (Double.parseDouble("0." + stringMinutesSecondes[1]) * 60.0);
		
		gameStatus += "(" + minutesGameLength + "m " + secondesGameLength + "s)";
		
		return gameStatus;
	}
	
}
