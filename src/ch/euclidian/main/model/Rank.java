package ch.euclidian.main.model;

public enum Rank {
  UNKNOWN("", -1),
  UNRANKED("", -1),
  I("1", 0),
  II("2", 100),
  III("3", 200),
  IV("4", 300);
  
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
