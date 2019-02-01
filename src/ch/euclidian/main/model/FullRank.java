package ch.euclidian.main.model;

import ch.euclidian.main.exception.NoValueRankException;

public class FullRank {

  private Rank rank;
  private Tier tier;
  private int leaguePoints;
  
  public FullRank(Rank rank, Tier tier, int leaguePoints) {
    this.rank = rank;
    this.tier = tier;
    this.leaguePoints = leaguePoints;
  }
  
  public int value() throws NoValueRankException {
    if(rank == Rank.UNRANKED || rank == Rank.UNKNOWN) {
      throw new NoValueRankException("Impossible to get Value of FullRank with Unranked or Unknown rank or tier");
    }
    return rank.getValue() + tier.getValue() + leaguePoints;
  }
  
  @Override
  public String toString() {
    if(rank == Rank.UNRANKED || rank == Rank.UNKNOWN) {
      return rank.toString();
    }
    return rank.toString() + " " + tier.toString() + " (" + leaguePoints + " LP)";
  }

  public Rank getRank() {
    return rank;
  }

  public void setRank(Rank rank) {
    this.rank = rank;
  }

  public Tier getTier() {
    return tier;
  }

  public void setTier(Tier tier) {
    this.tier = tier;
  }

  public int getLeaguePoints() {
    return leaguePoints;
  }

  public void setLeaguePoints(int leaguePoints) {
    this.leaguePoints = leaguePoints;
  }
  
}
