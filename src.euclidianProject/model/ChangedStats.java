package model;

public class ChangedStats {

	private StatsType type;
	private Number oldValue;
	private Number newValue;

	public ChangedStats(StatsType type, Number oldValue, Number newValue) {
		this.type = type;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public String toString() {
		switch (type) {

		case DURATION:

			int oldMinutes = (int) oldValue / 60;
			int oldSecondes = ((int) oldValue % 60) * 60; //TODO: Check data
			int newMinutes = (int) newValue / 60;
			int newSecondes = ((int) newValue % 60) * 60;

			if((int) oldValue < (int) newValue) {
				return "La durée moyenne de vos parties à augmentée de "
						+ oldMinutes + " min. " + oldSecondes + " s. à " + newMinutes + " min. " + newSecondes + " s.";
			}else if((int) oldValue > (int) newValue) {
				return "La durée moyenne de vos parties à été réduite de "
						+ newMinutes + " min. " + newSecondes + " s. à " + oldMinutes + " min. " + oldSecondes + " s.";
			}else {
				return "La durée moyenne de vos parties n'a pas changé, elle est de "
						+ newMinutes + " min. " + newSecondes + " s.";
			}

		case CHAMPIONS_PLAYED:

			if((int) oldValue < (int) newValue) {
				return "Vous avez joué " + ((int) newValue - (int) oldValue) + " champions de plus que la dernière fois.\n"
						+ "Semaine passé : " + (int) oldValue + " Champions différents joué.\n"
						+ "Cette semaine : " + (int) newValue + " Champions différents joué.";
			}else if((int) oldValue > (int) newValue) {
				return "Vous avez joué " + ((int) oldValue - (int) newValue) + " champions de moins que la dernière fois.\n "
						+ "Semaine passé : " + (int) oldValue + " Champions différents joué.\n"
						+ "Cette semaine : " + (int) newValue + " Champions différents joué.";
			}else {
				return "Vous avez joué autant de champions que la semaine passé. Champions différents joué cette semaine : " + (int) newValue;
			}

		case CREEP_AT_10:
			
			if((double) oldValue < (double) newValue) {
				return "Vous avez en moyenne " + ((double) newValue - (double) oldValue) + " creeps à 10 minutes de plus que la dernière fois.\n"
						+ "Semaine passé : " + (double) oldValue + " creeps à 10 minutes en moyenne.\n"
						+ "Cette semaine : " + (double) newValue + " creeps à 10 minutes en moyenne.";
			}else if((double) oldValue > (double) newValue) {
				return "Vous avez en moyenne " + ((double) oldValue - (double) newValue) + " creeps à 10 minutes de moins que la dernière fois.\n"
						+ "Semaine passé : " + (double) oldValue + " creeps à 10 minutes en moyenne.\n"
						+ "Cette semaine : " + (double) newValue + " creeps à 10 minutes en moyenne.";
			}else {
				return "Vous avez en moyenne autant de creeps à 10 minutes que la dernière fois. "
						+ "Moyenne de creeps à 10 minutes cette semaine : " + (double) newValue;
			}
			
		case CREEP_AT_20:
			
			if((double) oldValue < (double) newValue) {
				return "Vous avez en moyenne " + ((double) newValue - (double) oldValue) + " creeps à 20 minutes de plus que la dernière fois.\n"
						+ "Semaine passé : " + (double) oldValue + " creeps à 20 minutes en moyenne.\n"
						+ "Cette semaine : " + (double) newValue + " creeps à 20 minutes en moyenne.";
			}else if((double) oldValue > (double) newValue) {
				return "Vous avez en moyenne " + ((double) oldValue - (double) newValue) + " creeps à 20 minutes de moins que la dernière fois.\n"
						+ "Semaine passé : " + (double) oldValue + " creeps à 20 minutes en moyenne.\n"
						+ "Cette semaine : " + (double) newValue + " creeps à 20 minutes en moyenne.";
			}else {
				return "Vous avez en moyenne autant de creeps à 20 minutes que la dernière fois. "
						+ "Moyenne de creeps à 20 minutes cette semaine : " + (double) newValue;
			}
			
		case CREEP_AT_30:
			
			if((double) oldValue < (double) newValue) {
				return "Vous avez en moyenne " + ((double) newValue - (double) oldValue) + " creeps à 30 minutes de plus que la dernière fois.\n"
						+ "Semaine passé : " + (double) oldValue + " creeps à 30 minutes en moyenne.\n"
						+ "Cette semaine : " + (double) newValue + " creeps à 30 minutes en moyenne.";
			}else if((double) oldValue > (double) newValue) {
				return "Vous avez en moyenne " + ((double) oldValue - (double) newValue) + " creeps à 30 minutes de moins que la dernière fois.\n"
						+ "Semaine passé : " + (double) oldValue + " creeps à 30 minutes en moyenne.\n"
						+ "Cette semaine : " + (double) newValue + " creeps à 30 minutes en moyenne.";
			}else {
				return "Vous avez en moyenne autant de creeps à 30 minutes que la dernière fois. "
						+ "Moyenne de creeps à 30 minutes cette semaine : " + (double) newValue;
			}
			
		case KDA:
			
			if((double) oldValue < (double) newValue) {
				return "Vous avez en moyenne " + ((double) newValue - (double) oldValue) + " de KDA moyen supplémentaires par rapport à la dernière fois.\n"
						+ "Semaine passé : " + (double) oldValue + " de KDA moyen\n"
						+ "Cette semaine : " + (double) newValue + " de KDA moyen";
			}else if((double) oldValue > (double) newValue) {
				return "Vous avez en moyenne " + ((double) oldValue - (double) newValue) + " de KDA moyen en moins par rapport à la dernière fois.\n"
						+ "Semaine passé : " + (double) oldValue + " de KDA moyen.\n"
						+ "Cette semaine : " + (double) newValue + " de KDA moyen.";
			}else{
				return "Vous avez en moyenne un KDA moyen égale à la dernière fois. "
						+ "KDA moyen cette semaine : " + (double) newValue;
			}
			
		default:
			return "Statistques inaffichable x(";
		}
	}
}
