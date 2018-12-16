package model;

import java.util.List;

import com.merakianalytics.orianna.types.core.summoner.Summoner;

import net.dv8tion.jda.core.entities.User;

public class Player {
	private String name;
	private User discordUser;
	private Summoner summoner;
	private List<PlayerDataOfTheWeek> listDataOfWeek;
	
	public Player(String name, User discordUser, Summoner summoner) {
		this.name = name;
		this.discordUser = discordUser;
		this.summoner = summoner;
		}

	public double getListMoyenneWinrate(){
		int totaleWin = 0;
		int totalGame = 0;
		
		for (int i = 0; i < listDataOfWeek.size(); i++) {
			totaleWin += listDataOfWeek.get(i).getNbrWin();
			totalGame += listDataOfWeek.get(i).getNbrGames();
		}
		
		if(totaleWin == 0) {
			return 0.0;
		}else {
			return (totaleWin / (double) totalGame) * 100;
		}
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

	public List<PlayerDataOfTheWeek> getListDataOfWeek() {
		return listDataOfWeek;
	}

	public void setListDataOfWeek(List<PlayerDataOfTheWeek> listDataOfWeek) {
		this.listDataOfWeek = listDataOfWeek;
	}
	
}
