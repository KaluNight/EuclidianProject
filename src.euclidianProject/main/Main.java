package main;

import java.util.ArrayList;

import model.Player;
import model.Team;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.managers.GuildController;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;

public class Main {
	
	private static JDA jda;
	
	private static RiotApi api;
	
	private static ArrayList<Team> teamList = new ArrayList<Team>();
	
	private static ArrayList<Player> playerList = new ArrayList<Player>();
	
	private static Guild guild;
	
	private static GuildController controller;
	
	public static void main(String[] args) {
		try {
			jda = new JDABuilder(AccountType.BOT).setToken(args[0]).build();
		} catch (IndexOutOfBoundsException e) {
			System.err.println("You must provide a token.");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		jda.addEventListener(new EventListener());
		
		ApiConfig config = new ApiConfig().setKey(args[1]);
		api = new RiotApi(config);
	}

	public static JDA getJda() {
		return jda;
	}

	public static void setJda(JDA jda) {
		Main.jda = jda;
	}

	public static RiotApi getApi() {
		return api;
	}

	public static ArrayList<Team> getTeamList() {
		return teamList;
	}

	public static void setTeamList(ArrayList<Team> teamList) {
		Main.teamList = teamList;
	}

	public static ArrayList<Player> getPlayerList() {
		return playerList;
	}

	public static void setPlayerList(ArrayList<Player> playerList) {
		Main.playerList = playerList;
	}

	public static Guild getGuild() {
		return guild;
	}

	public static void setGuild(Guild guild) {
		Main.guild = guild;
	}

	public static GuildController getController() {
		return controller;
	}

	public static void setController(GuildController controller) {
		Main.controller = controller;
	}
}
