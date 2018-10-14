package model;

import net.dv8tion.jda.core.entities.User;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;

public class Player {
	private String name;
	private User discordUser;
	private Summoner summoner;
	
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
	
}
