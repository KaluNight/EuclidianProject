package model;

import java.util.ArrayList;

import org.joda.time.Duration;

import com.merakianalytics.orianna.types.core.staticdata.Champion;
import com.merakianalytics.orianna.types.core.staticdata.SummonerSpell;

public class PlayerDataOfTheWeek {

	private ArrayList<Duration> listeDuration;
	private ArrayList<Integer> listTotCreep10Minute;
	private ArrayList<Integer> listTotCreep20Minute;
	private ArrayList<Integer> listTotCreep30Minute;
	private ArrayList<SummonerSpell> listOfSummonerSpellUsed;
	private ArrayList<KDA> listOfKDA;
	private ArrayList<Champion> listOfChampionPlayed;

	private int nbrGames = 0;
	private int nbrWin = 0;

	public PlayerDataOfTheWeek() {
		
	}

	public ArrayList<Duration> getListeDuration() {
		return listeDuration;
	}
	public void setListeDuration(ArrayList<Duration> listeDuration) {
		this.listeDuration = listeDuration;
	}
	public ArrayList<Integer> getListTotCreep10Minute() {
		return listTotCreep10Minute;
	}
	public void setListTotCreep10Minute(ArrayList<Integer> listTotCreep10Minute) {
		this.listTotCreep10Minute = listTotCreep10Minute;
	}
	public ArrayList<Integer> getListTotCreep20Minute() {
		return listTotCreep20Minute;
	}
	public void setListTotCreep20Minute(ArrayList<Integer> listTotCreep20Minute) {
		this.listTotCreep20Minute = listTotCreep20Minute;
	}
	public ArrayList<Integer> getListTotCreep30Minute() {
		return listTotCreep30Minute;
	}
	public void setListTotCreep30Minute(ArrayList<Integer> listTotCreep30Minute) {
		this.listTotCreep30Minute = listTotCreep30Minute;
	}
	public ArrayList<SummonerSpell> getListOfSummonerSpellUsed() {
		return listOfSummonerSpellUsed;
	}
	public void setListOfSummonerSpellUsed(ArrayList<SummonerSpell> listOfSummonerSpellUsed) {
		this.listOfSummonerSpellUsed = listOfSummonerSpellUsed;
	}
	public ArrayList<KDA> getListOfKDA() {
		return listOfKDA;
	}
	public void setListOfKDA(ArrayList<KDA> listOfKDA) {
		this.listOfKDA = listOfKDA;
	}
	public ArrayList<Champion> getListOfChampionPlayed() {
		return listOfChampionPlayed;
	}
	public void setListOfChampionPlayed(ArrayList<Champion> listOfChampionPlayed) {
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

}
