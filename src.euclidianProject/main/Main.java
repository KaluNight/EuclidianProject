package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import model.Player;
import model.Team;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.managers.GuildController;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;

public class Main {

	private static final File SAVE_FILE = new File("ressources/save");


	//------------------------------

	private static JDA jda;

	private static RiotApi api;

	//-------------------------------

	private static ArrayList<Team> teamList = new ArrayList<Team>();

	private static ArrayList<Player> playerList = new ArrayList<Player>();

	private static Role registeredRole;

	//-------------------------------

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

	public static void saveData() throws IOException {
		FileOutputStream fos = new FileOutputStream(SAVE_FILE); //Make id System
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		try {

			oos.writeInt(playerList.size()); //Number of Player

			for(int i = 0; i < playerList.size(); i++) {
				Player player = playerList.get(i);

				oos.writeUTF(player.getName()); //Name
				oos.writeUTF(player.getDiscordUser().getId()); //Discord ID

				oos.writeObject(player.getSummoner()); //Summoner Object
			}

			oos.writeInt(teamList.size()); //Number of Teams

			for(int i = 0; i < teamList.size(); i++) {
				Team team = teamList.get(i);

				oos.writeUTF(team.getName()); //Name
				oos.writeUTF(team.getRole().getId()); //ID of role
				oos.writeUTF(team.getCategory().getId()); //ID of category
				
				oos.writeInt(team.getPlayers().size()); //Team size

				for(int j = i; j < team.getPlayers().size(); j++) {
					oos.writeUTF(team.getPlayers().get(i).getDiscordUser().getId()); //Write Discord ID of players
				}
			}

		} finally {
			oos.close();
		}
	}

	public static void loadData() throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(SAVE_FILE);
		ObjectInputStream ois = new ObjectInputStream(fis);
		try {

			int playerNbr = ois.readInt();

			for(int i = 0; i < playerNbr; i++) {

				String name = ois.readUTF();

				String discordID = ois.readUTF();
				User user = jda.getUserById(discordID);

				Summoner summoner = (Summoner) ois.readObject();

				playerList.add(new Player(name, user, summoner));
			}


			int teamNbr = ois.readInt();

			for(int i = 0; i < teamNbr; i++) {
				String name = ois.readUTF();
				String roleID = ois.readUTF();
				Role role = guild.getRoleById(roleID);

				String categoryID = ois.readUTF();
				Category category = guild.getCategoryById(categoryID);

				Team team = new Team(name, category, role);

				int playerListSize = ois.readInt();

				ArrayList<Player> players = new ArrayList<Player>();
				for(int j = 0; j < playerListSize; j++) {
					String discordID = ois.readUTF();
					players.add(getPlayersByDiscordId(discordID));
				}
				team.setPlayers(players);

				teamList.add(team);
			}

		} finally {
			ois.close();
		}
	}

	public static Player getPlayersByDiscordId(String id) {
		for(int i = 0; i < playerList.size(); i++) {
			if(playerList.get(i).getDiscordUser().getId().equals(id)) {
				return playerList.get(i);
			}
		}
		return null;
	}

	public static Team getTeamByName(String name) {
		for(int i = 0; i < teamList.size(); i++) {
			if(teamList.get(i).getName().equalsIgnoreCase(name)) {
				return teamList.get(i);
			}
		}
		return null;
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

	public static Role getRegisteredRole() {
		return registeredRole;
	}

	public static void setRegisteredRole(Role registeredRole) {
		Main.registeredRole = registeredRole;
	}
}
