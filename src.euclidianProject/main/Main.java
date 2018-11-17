package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import model.Player;
import model.Postulation;
import model.Team;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.managers.GuildController;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;

public class Main {

	private static final File SAVE_FILE = new File("ressources/save");

	//------------------------------
	
	private static JDA jda;

	private static RiotApi riotApi;

	//-------------------------------

	private static ArrayList<Team> teamList = new ArrayList<Team>();

	private static ArrayList<Player> playerList = new ArrayList<Player>();

	private static ArrayList<Postulation> postulationsList = new ArrayList<Postulation>();
	
	private static ArrayList<String> reportList = new ArrayList<String>();

	private static Role registeredRole;

	private static Role postulantRole;

	private static ArrayList<Role> rolePosition;

	//-------------------------------

	private static Guild guild;

	private static GuildController controller;
	
	//-------------------------------
	
	private static TextChannel logBot;
	

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

		ApiConfig config = new ApiConfig();
		config.setKey(args[1]);
		riotApi = new RiotApi(config);
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

			oos.writeInt(postulationsList.size()); //Number of Postulation

			for(int i = 0; i < postulationsList.size(); i++) {
				Postulation postulation = postulationsList.get(i);

				oos.writeUTF(postulation.getMember().getUser().getId());
				oos.writeObject(postulation.getSummoner());

				oos.writeInt(postulation.getRoles().size()); //Number of roles
				for(int j = 0; j < postulation.getRoles().size(); j++) {
					oos.writeUTF(postulation.getRoles().get(j).getId());
				}

				oos.writeUTF(postulation.getHoraires());
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

			int postulationNbr;
			try {
				postulationNbr = ois.readInt();
			} catch (Exception e) {
				postulationNbr = 0;
			}

			for(int i = 0; i < postulationNbr; i++) {
				String userId = ois.readUTF();
				Member member = guild.getMemberById(userId);

				Summoner summoner = (Summoner) ois.readObject();

				ArrayList<Role> roles = new ArrayList<Role>();

				int roleNmbr = ois.readInt();
				for(int j = 0; j < roleNmbr; j++) {
					String roleId = ois.readUTF();
					roles.add(guild.getRoleById(roleId));
				}

				String horaires = ois.readUTF();

				postulationsList.add(new Postulation(member, summoner, roles, horaires));
			}

		} finally {
			ois.close();
		}
	}

	public static int getPostulationIndexByMember(Member member) {
		for(int i = 0; i < postulationsList.size(); i++) {
			if(postulationsList.get(i).getMember().equals(member)) {
				return i;
			}
		}
		return -1;
	}

	public static Role getPositionRoleByName(String str) {
		for(int i = 0; i < rolePosition.size(); i++) {
			if(rolePosition.get(i).getName().equalsIgnoreCase(str)) {
				return rolePosition.get(i);
			}
		}
		return null;
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
	
	public static void addReport(String report) {
		reportList.add(report);
	}

	public static JDA getJda() {
		return jda;
	}

	public static void setJda(JDA jda) {
		Main.jda = jda;
	}

	public static RiotApi getRiotApi() {
		return riotApi;
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

	public static ArrayList<Role> getRolePosition() {
		return rolePosition;
	}

	public static void setRolePosition(ArrayList<Role> rolePosition) {
		Main.rolePosition = rolePosition;
	}

	public static ArrayList<Postulation> getPostulationsList() {
		return postulationsList;
	}

	public static void setPostulationsList(ArrayList<Postulation> postulationsList) {
		Main.postulationsList = postulationsList;
	}

	public static Role getPostulantRole() {
		return postulantRole;
	}

	public static void setPostulantRole(Role postulantRole) {
		Main.postulantRole = postulantRole;
	}

	public static TextChannel getLogBot() {
		return logBot;
	}

	public static void setLogBot(TextChannel logBot) {
		Main.logBot = logBot;
	}

	public static ArrayList<String> getReportList() {
		return reportList;
	}

	public static void setReportList(ArrayList<String> reportList) {
		Main.reportList = reportList;
	}
}
