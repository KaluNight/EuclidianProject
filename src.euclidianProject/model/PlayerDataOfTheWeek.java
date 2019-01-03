package model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Duration;

public class PlayerDataOfTheWeek {

  private String weekStart; //ISO 8601
  private String weekEnd; //ISO 8601
  private List<Duration> listeDuration;
  private List<Double> listTotCreep10Minute;
  private List<Double> listTotCreep20Minute;
  private List<Double> listTotCreep30Minute;
  private List<Integer> listOfSummonerSpellUsedId;
  private List<KDA> listOfKDA;
  private List<Integer> listOfChampionPlayedId;

  private int nbrGames = 0;
  private int nbrWin = 0;

  public PlayerDataOfTheWeek(String weekStart, String weekEnd) {
    this.weekStart = weekStart;
    this.weekEnd = weekEnd;
  }

  public double getWinRateOfTheWeek() {
    if(nbrGames == 0){
      return 0;
    }
    return (nbrWin / (double) nbrGames) * 100;
  }

  public double getKDAOfTheWeek(){
    double totalKdaScores = 0;

    for(int i = 0; i < listOfKDA.size(); i++) {
      totalKdaScores += listOfKDA.get(i).getKDAScores();
    }

    if(listOfKDA.size() == 0) {
      return 0.0;
    }
    return totalKdaScores / listOfKDA.size();
  }

  public int getAverageDurationOfTheWeek() {
    int totalDuration = 0;

    for(int i = 0; i < listeDuration.size(); i++) {
      totalDuration += listeDuration.get(i).getStandardSeconds();
    }

    if(listeDuration.size() == 0) {
      return 0;
    }

    return totalDuration / listeDuration.size();
  }

  public double getAverageCreepsAt10() {
    int totalCreep = 0;

    for(int i = 0; i < listTotCreep10Minute.size(); i++) {
      totalCreep += listTotCreep10Minute.get(i);
    }

    if(listTotCreep10Minute.size() == 0) {
      return 0;
    }

    return totalCreep / listTotCreep10Minute.size();
  }

  public double getAverageCreepsAt20() {
    int totalCreep = 0;

    for(int i = 0; i < listTotCreep20Minute.size(); i++) {
      totalCreep += listTotCreep20Minute.get(i);
    }

    if(listTotCreep20Minute.size() == 0) {
      return 0;
    }

    return totalCreep / listTotCreep20Minute.size();
  }

  public double getAverageCreepsAt30() {
    int totalCreep = 0;

    for(int i = 0; i < listTotCreep30Minute.size(); i++) {
      totalCreep += listTotCreep30Minute.get(i);
    }

    if(listTotCreep30Minute.size() == 0) {
      return 0;
    }

    return totalCreep / listTotCreep30Minute.size();
  }

  public int getNumberOfDifferentChampionsPlayed() {
    ArrayList<Integer> championTreated = new ArrayList<>();

    for(int i = 0; i < listOfChampionPlayedId.size(); i++) {
      int actualID = listOfChampionPlayedId.get(i);

      boolean championIsInTheList = false;
      for(int j = 0; j < championTreated.size(); j++) {
        if(championTreated.get(j) == actualID) {
          championIsInTheList = true;
          break;
        }
      }

      if(!championIsInTheList) {
        championTreated.add(actualID);
      }
    }

    return championTreated.size();
  }



  public List<Duration> getListeDuration() {
    return listeDuration;
  }
  public void setListeDuration(List<Duration> listeDuration) {
    this.listeDuration = listeDuration;
  }
  public List<Double> getListTotCreep10Minute() {
    return listTotCreep10Minute;
  }
  public void setListTotCreep10Minute(List<Double> listTotCreep10Minute) {
    this.listTotCreep10Minute = listTotCreep10Minute;
  }
  public void setListTotCreep20Minute(List<Double> listTotCreep20Minute) {
    this.listTotCreep20Minute = listTotCreep20Minute;
  }
  public List<Double> getListTotCreep30Minute() {
    return listTotCreep30Minute;
  }
  public void setListTotCreep30Minute(List<Double> listTotCreep30Minute) {
    this.listTotCreep30Minute = listTotCreep30Minute;
  }
  public List<Integer> getListOfSummonerSpellUsed() {
    return listOfSummonerSpellUsedId;
  }
  public void setListOfSummonerSpellUsed(List<Integer> listOfSummonerSpellUsed) {
    this.listOfSummonerSpellUsedId = listOfSummonerSpellUsed;
  }
  public List<KDA> getListOfKDA() {
    return listOfKDA;
  }
  public void setListOfKDA(List<KDA> listOfKDA) {
    this.listOfKDA = listOfKDA;
  }
  public List<Integer> getListOfChampionPlayed() {
    return listOfChampionPlayedId;
  }
  public void setListOfChampionPlayed(List<Integer> listOfChampionPlayed) {
    this.listOfChampionPlayedId = listOfChampionPlayed;
  }
  public int getNbrGames() {
    return nbrGames;
  }
  public void setNbrGames(int nbrGames) {
    this.nbrGames = nbrGames;
  }
  public int getNbrWin() {
    return nbrWin;
  }
  public void setNbrWin(int nbrWin) {
    this.nbrWin = nbrWin;
  }

  public String getWeekStart() {
    return weekStart;
  }

  public void setWeekStart(String weekStart) {
    this.weekStart = weekStart;
  }

  public String getWeekEnd() {
    return weekEnd;
  }

  public void setWeekEnd(String weekEnd) {
    this.weekEnd = weekEnd;
  }

  public List<Double> getListTotCreep20Minute() {
    return listTotCreep20Minute;
  }

}
