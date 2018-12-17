package model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.merakianalytics.orianna.types.core.staticdata.Champion;
import com.merakianalytics.orianna.types.core.staticdata.SummonerSpell;

public class PlayerDataOfTheWeek {

	private DateTime weekStart;
	private DateTime weekEnd;
	private List<Duration> listeDuration;
	private List<Integer> listTotCreep10Minute;
	private List<Integer> listTotCreep20Minute;
	private List<Integer> listTotCreep30Minute;
	private List<SummonerSpell> listOfSummonerSpellUsed;
	private List<KDA> listOfKDA;
	private List<Champion> listOfChampionPlayed;

	private int nbrGames = 0;
	private int nbrWin = 0;

	public PlayerDataOfTheWeek(DateTime weekStart, DateTime weekEnd) {
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
		
		for(int i = 0; i < listOfChampionPlayed.size(); i++) {
			int actualID = listOfChampionPlayed.get(i).getId();
			
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
	public List<Integer> getListTotCreep10Minute() {
		return listTotCreep10Minute;
	}
	public void setListTotCreep10Minute(List<Integer> listTotCreep10Minute) {
		this.listTotCreep10Minute = listTotCreep10Minute;
	}
	public void setListTotCreep20Minute(List<Integer> listTotCreep20Minute) {
		this.listTotCreep20Minute = listTotCreep20Minute;
	}
	public List<Integer> getListTotCreep30Minute() {
		return listTotCreep30Minute;
	}
	public void setListTotCreep30Minute(List<Integer> listTotCreep30Minute) {
		this.listTotCreep30Minute = listTotCreep30Minute;
	}
	public List<SummonerSpell> getListOfSummonerSpellUsed() {
		return listOfSummonerSpellUsed;
	}
	public void setListOfSummonerSpellUsed(List<SummonerSpell> listOfSummonerSpellUsed) {
		this.listOfSummonerSpellUsed = listOfSummonerSpellUsed;
	}
	public List<KDA> getListOfKDA() {
		return listOfKDA;
	}
	public void setListOfKDA(List<KDA> listOfKDA) {
		this.listOfKDA = listOfKDA;
	}
	public List<Champion> getListOfChampionPlayed() {
		return listOfChampionPlayed;
	}
	public void setListOfChampionPlayed(List<Champion> listOfChampionPlayed) {
		this.listOfChampionPlayed = listOfChampionPlayed;
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

	public DateTime getWeekStart() {
		return weekStart;
	}

	public void setWeekStart(DateTime weekStart) {
		this.weekStart = weekStart;
	}

	public DateTime getWeekEnd() {
		return weekEnd;
	}

	public void setWeekEnd(DateTime weekEnd) {
		this.weekEnd = weekEnd;
	}

	public List<Integer> getListTotCreep20Minute() {
		return listTotCreep20Minute;
	}

}
