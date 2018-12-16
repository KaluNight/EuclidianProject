package model;

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
	public List<Integer> getListTotCreep20Minute() {
		return listTotCreep20Minute;
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

}
