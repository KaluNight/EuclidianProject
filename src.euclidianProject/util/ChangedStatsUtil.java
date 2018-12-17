package util;

import model.ChangedStats;
import model.StatsType;

public class ChangedStatsUtil {

	public static String getName(StatsType statsType) {
		switch (statsType) {
		case CHAMPIONS_PLAYED:
			return "Champions différents joué";
		case CREEP_AT_10:
			return "Sbires tué à 10 minutes";
		case CREEP_AT_20:
			return "Sbires tué à 20 minutes";
		case CREEP_AT_30:
			return "Sbires tué à 30 minutes";
		case DURATION:
			break;
		case KDA:
			break;
		case SUMMONER_TYPE:
			break;
		default:
			break;
		
		}
	}
	
}
