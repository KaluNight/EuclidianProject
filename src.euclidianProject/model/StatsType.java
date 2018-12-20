package model;

public enum StatsType {
	DURATION("Durée de la partie"),
	CREEP_AT_10("Sbires tués à 10 minutes"),
	CREEP_AT_20("Sbires tués à 20 minutes"),
	CREEP_AT_30("Sbires tués à 30 minutes"),
	SUMMONER_TYPE("Type de sort d'invocateur"),
	KDA("KDA"),
	CHAMPIONS_PLAYED("Champions différents joué");
	
	private String nameValue;
  
	StatsType(String nameValue){
    this.nameValue = nameValue;
}
	
  @Override
  public String toString() {
    return nameValue;
  }
}
