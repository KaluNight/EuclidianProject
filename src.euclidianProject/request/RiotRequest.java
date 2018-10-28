package request;

import java.util.Iterator;
import java.util.Set;

import main.Main;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.league.dto.LeaguePosition;
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
	
}
