package util;

import model.StatsType;

public class ChangedStatsUtil {

	private ChangedStatsUtil() {
	}
	
	public static String getName(StatsType statsType) {
		switch (statsType) {
		case CHAMPIONS_PLAYED:
			return "Champions différents joué";
		case CREEP_AT_10:
			return "Sbires tués à 10 minutes";
		case CREEP_AT_20:
			return "Sbires tués à 20 minutes";
		case CREEP_AT_30:
			return "Sbires tués à 30 minutes";
		case DURATION:
			return "Durée de la partie";
		case KDA:
			return "KDA";
		case SUMMONER_TYPE:
			return "Type de sort d'invocateur";
		default:
			return "Type de données inconnu";
		}
	}
}
