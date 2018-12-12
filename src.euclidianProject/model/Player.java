package model;

import java.util.ArrayList;

import com.merakianalytics.orianna.types.core.summoner.Summoner;

import net.dv8tion.jda.core.entities.User;

public class Player {
	private String name;
	private User discordUser;
	private Summoner summoner;
	private ArrayList<PlayerDataOfTheWeek> listDataOfWeek;
	
	public Player(String name, User discordUser, Summoner summoner) {
		this.name = name;
		this.discordUser = discordUser;
		this.summoner = summoner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getDiscordUser() {
		return discordUser;
	}

	public void setDiscordUser(User discordUser) {
		this.discordUser = discordUser;
	}

	public Summoner getSummoner() {
		return summoner;
	}

	public void setSummoner(Summoner summoner) {
		this.summoner = summoner;
	}

	public ArrayList<PlayerDataOfTheWeek> getListDataOfWeek() {
		return listDataOfWeek;
	}

	public void setListDataOfWeek(ArrayList<PlayerDataOfTheWeek> listDataOfWeek) {
		this.listDataOfWeek = listDataOfWeek;
	}
	
}
