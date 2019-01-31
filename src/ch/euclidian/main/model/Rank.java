package ch.euclidian.main.model;

public enum Rank {
  UNKNOWN("Inconnu", -1),
  UNRANKED("Unranked", -1),
  IRON("Fer", 1000),
  BRONZE("Bronze", 1400),
  SILVER("Argent", 1800),
  GOLD("Or", 2200),
  PLATINUM("Platine", 2600),
  DIAMANT("Diamant", 3000),
  MASTER("Maître", 3400),
  GRAND_MASTER("Grand Maître", 3800),
  CHALLENGER("Challenger", 4200);
  
  private String name;
  private int value;

  Rank(String name, int value) {
    this.name = name;
    this.value = value;
  }

  public int getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return name;
  }
}
