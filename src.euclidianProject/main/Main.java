package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class Main {

	private static final File SAVE_TXT_FILE = new File("ressources/save.txt");

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

	public static void saveDataTxt() throws FileNotFoundException, UnsupportedEncodingException {

		StringBuilder saveString = new StringBuilder();

		saveString.append("//Liste Of Player\n\n");

		for(int i = 0; i < playerList.size(); i++) {
			Player player = playerList.get(i);

			saveString.append("--p\n");

			saveString.append(player.getName() + "\n");
			saveString.append(player.getDiscordUser().getId() + "\n");
			saveString.append(player.getSummoner().getAccountId() + "\n\n");
		}

		saveString.append("\n//Liste of teams\n");

		for(int i = 0; i < teamList.size(); i++) {
			Team team = teamList.get(i);

			saveString.append("--t\n");

			saveString.append(team.getName() + "\n");
			saveString.append(team.getRole().getId() + "\n");
			saveString.append(team.getCategory().getId() + "\n");
			saveString.append(team.getPlayers().size() + "\n");

			for(int j = 0; j < team.getPlayers().size(); j++) {
				saveString.append(team.getPlayers().get(j).getDiscordUser().getId() + "\n");
			}
			saveString.append("\n");
		}

		saveString.append("\n\n//Postulations\n");

		for(int i = 0; i < postulationsList.size(); i++) {
			Postulation postulation = postulationsList.get(i);

			saveString.append("--post\n");

			saveString.append(postulation.getMember().getUser().getId() + "\n");
			saveString.append(postulation.getSummoner().getAccountId() + "\n");
			saveString.append(postulation.getRoles().size() + "\n");

			for(int j = 0; j < postulation.getRoles().size(); j++) {
				saveString.append(postulation.getRoles().get(j).getId() + "\n");
			}
			
			saveString.append(postulation.getHoraires());
			
			saveString.append("\n");
		}

		saveString.append("\n\n//Report\n");

		for(int i = 0; i < reportList.size(); i++) {
			saveString.append("--r\n");

			saveString.append(reportList.get(i));
			saveString.append("--end");
		}

		PrintWriter writer = null;

		try {
			writer = new PrintWriter(SAVE_TXT_FILE ,"UTF-8");
			writer.write(saveString.toString());
		} finally {
			if(writer != null) {
				writer.close();
			}
		}
	}

	public static void loadDataTxt() throws IOException, RiotApiException {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(SAVE_TXT_FILE));
			String line;

			while((line = reader.readLine()) != null) {

				if(line.equals("--p")) {
					String discordName = reader.readLine();
					String discordID = reader.readLine();
					long accountId = Long.parseLong(reader.readLine());

					if(!isPlayersAlreadyCopied(discordID)) {
						User user = jda.getUserById(discordID);
						Summoner summoner = riotApi.getSummonerByAccount(Platform.EUW, accountId);

						playerList.add(new Player(discordName, user, summoner));
					}

				} else if (line.equals("--t")) {

					String teamName = reader.readLine();

					String roleId = reader.readLine();
					Role role = guild.getRoleById(roleId);


					String categoryId = reader.readLine();
					Category category = guild.getCategoryById(categoryId);

					Team team = new Team(teamName, category, role);

					int numberOfPlayer = Integer.parseInt(reader.readLine());

					ArrayList<Player> players = new ArrayList<Player>();
					for(int i = 0; i < numberOfPlayer; i++) {
						players.add(getPlayersByDiscordId(reader.readLine()));
					}

					team.setPlayers(players);
					teamList.add(team);

				} else if (line.equals("--post")){

					String userId = reader.readLine();
					Member member = guild.getMemberById(userId);

					Summoner summoner = riotApi.getSummonerByAccount(Platform.EUW, Long.parseLong(reader.readLine()));


					ArrayList<Role> roles = new ArrayList<Role>();

					int roleNmbr = Integer.parseInt(reader.readLine());
					for(int j = 0; j < roleNmbr; j++) {
						String roleId = reader.readLine();
						roles.add(guild.getRoleById(roleId));
					}

					String horaires = reader.readLine();

					Postulation postulation = new Postulation(member, summoner, roles, horaires);
					postulationsList.add(postulation);

				} else if (line.equals("--r")) {

					StringBuilder stringBuilder = new StringBuilder();

					while(true) {
						line = reader.readLine();

						if(line.equals("--stop")) {
							break;
						} else {
							stringBuilder.append(line);
						}
					}

					reportList.add(stringBuilder.toString());
				}
			}

		} finally {
			reader.close();
		}
	}

	private static boolean isPlayersAlreadyCopied(String discordUserId) {
		for(int i = 0; i < playerList.size(); i++) {
			if(playerList.get(i).getDiscordUser().getId().equals(discordUserId)) {
				return true;
			}
		}

		return false;
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
