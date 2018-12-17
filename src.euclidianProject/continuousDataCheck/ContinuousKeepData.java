package continuousDataCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import com.merakianalytics.orianna.types.core.match.Match;
import com.merakianalytics.orianna.types.core.match.MatchHistory;
import com.merakianalytics.orianna.types.core.match.Participant;
import com.merakianalytics.orianna.types.core.searchable.SearchableList;
import com.merakianalytics.orianna.types.core.staticdata.Champion;
import com.merakianalytics.orianna.types.core.staticdata.SummonerSpell;
import com.merakianalytics.orianna.types.core.summoner.Summoner;
import main.Main;
import model.ChangedStats;
import model.KDA;
import model.Player;
import model.PlayerDataOfTheWeek;
import model.StatsType;
import net.dv8tion.jda.core.entities.TextChannel;

public class ContinuousKeepData extends TimerTask{

	private static int indexPlayerSelected = -1;
	
	private static DateTime weekDateStart; //TODO: Date need to be set Every week
	
	private static DateTime weekDateEnd; //TODO: Date need to be set Every week
	
	private static TextChannel statsChannel; //TODO: Initialize
	
	//TODO: Object for stack treated history of player
	
	//IDEA: Do a treatment of data each end of day (?) for prevent chaine lose after 3 lose ?
	
	/**
	 * Run this every end of week. (TODO)
	 * Get All player history of in the week and return a PlayerDataObject (TODO)
	 */
	@Override
	public void run() {
		
		statsChannel.sendTyping().complete();
		statsChannel.sendMessage("__**Début d'analyse Hebdomadaire**__").complete();
		
		for(int i = 0; i < Main.getPlayerList().size(); i++) {
			if((indexPlayerSelected + 1) >= Main.getPlayerList().size()) {
				indexPlayerSelected = 0;
			}else {
				indexPlayerSelected++;
			}
			
			Player player = Main.getPlayerList().get(indexPlayerSelected);
			Summoner summoner = player.getSummoner();
			
			statsChannel.sendMessage("**Rapport pour " + player.getDiscordUser().getAsMention() + " sur le compte " + summoner.getName() + ".**").complete();
			statsChannel.sendTyping().complete();
			
			MatchHistory matchHistory = MatchHistory.forSummoner(summoner).withStartTime(weekDateStart).withEndTime(weekDateEnd).get();
			
			PlayerDataOfTheWeek playerDataOfTheWeek = getDataFromTheHistory(matchHistory, player.getSummoner());
			
			player.getListDataOfWeek().add(playerDataOfTheWeek);
			Main.getPlayerList().remove(indexPlayerSelected);
			Main.getPlayerList().add(player); //Check if copy
			
			List<ChangedStats> changedStats = generatingStats(player);
			
			if(!changedStats.isEmpty()) {
				sendReport(player, changedStats);
			}
			
			
		}
	}
	
	private void sendReport(Player player, List<ChangedStats> changedStats) {
		
		for (int i = 0; i < changedStats.size(); i++) {
			statsChannel.sendTyping().queue();
			
			ChangedStats stats = changedStats.get(i);
			
			
					
		}
	}
	
	private List<ChangedStats> generatingStats(Player player) {
		//TODO: check value different value with ChangedStats
		if(player.getListDataOfWeek().size() == 1) {
			statsChannel.sendMessage("C'est la première fois que vos donnés sont analysé, vous aurez un rapport la semaine prochaine.").complete();
			return new ArrayList<>();
		}else if(player.getListDataOfWeek().size() == 0) {
			statsChannel.sendMessage("Vos données n'ont pas pu être analysé normalement, un dev va s'occuper de votre cas :x").complete();
			return new ArrayList<>();
		}else {
			PlayerDataOfTheWeek lastWeek = player.getListDataOfWeek().get(player.getListDataOfWeek().size() - 1);
			PlayerDataOfTheWeek thisWeek = player.getListDataOfWeek().get(player.getListDataOfWeek().size() - 2);
			
			ArrayList<ChangedStats> listOfChangedStats = new ArrayList<>();
			
			listOfChangedStats.add(new ChangedStats(StatsType.DURATION, lastWeek.getAverageDurationOfTheWeek(), thisWeek.getAverageDurationOfTheWeek()));
			listOfChangedStats.add(new ChangedStats(StatsType.CREEP_AT_10, lastWeek.getAverageCreepsAt10(), thisWeek.getAverageCreepsAt10()));
			listOfChangedStats.add(new ChangedStats(StatsType.CREEP_AT_20, lastWeek.getAverageCreepsAt20(), thisWeek.getAverageCreepsAt20()));
			listOfChangedStats.add(new ChangedStats(StatsType.CREEP_AT_30, lastWeek.getAverageCreepsAt30(), thisWeek.getAverageCreepsAt30()));
			listOfChangedStats.add(new ChangedStats(StatsType.KDA, lastWeek.getKDAOfTheWeek(), thisWeek.getKDAOfTheWeek()));
			listOfChangedStats.add(new ChangedStats(StatsType.SUMMONER_TYPE, lastWeek.getNumberOfDifferentChampionsPlayed(), thisWeek.getNumberOfDifferentChampionsPlayed()));
			
			return listOfChangedStats;
		}
	}
	
	private PlayerDataOfTheWeek getDataFromTheHistory(MatchHistory matchHistory, Summoner summoner) {
		ArrayList<Duration> listeDuration = new ArrayList<>();
		ArrayList<Integer> listTotCreep10Minute = new ArrayList<>();
		ArrayList<Integer> listTotCreep20Minute = new ArrayList<>();
		ArrayList<Integer> listTotCreep30Minute = new ArrayList<>();
		ArrayList<SummonerSpell> listOfSummonerSpellUsed = new ArrayList<>();
		ArrayList<KDA> listOfKDA = new ArrayList<>();
		ArrayList<Champion> listOfChampionPlayed = new ArrayList<>();
		
		int nbrGames = 0;
		int nbrWin = 0;
		
		for(int i = 0; i < matchHistory.size(); i++) {
			Match match = matchHistory.get(i);
			
			listeDuration.add(match.getDuration());
			SearchableList<Participant> participants = match.getBlueTeam().getParticipants();
			
			Participant participant = participants.search(summoner.getAccountId()).get(0);
			
			listTotCreep10Minute.add((int) participant.getTimeline().getCreepScore().getAt10());
			listTotCreep20Minute.add((int) participant.getTimeline().getCreepScore().getAt20());
			listTotCreep30Minute.add((int) participant.getTimeline().getCreepScore().getAt30());
			
			listOfSummonerSpellUsed.add(participant.getSummonerSpellD());
			listOfSummonerSpellUsed.add(participant.getSummonerSpellF());
			
			int kills = participant.getStats().getKills();
			int deaths = participant.getStats().getDeaths();
			int assists = participant.getStats().getAssists();
			listOfKDA.add(new KDA(kills, deaths, assists));
			
			listOfChampionPlayed.add(participant.getChampion());
			
			nbrGames++;
			if(participant.getTeam().isWinner()) {
				nbrWin++;
			}
		}
		
		PlayerDataOfTheWeek playerDataOfTheWeek = new PlayerDataOfTheWeek(weekDateStart, weekDateEnd);
		
		playerDataOfTheWeek.setListeDuration(listeDuration);
		playerDataOfTheWeek.setListOfChampionPlayed(listOfChampionPlayed);
		playerDataOfTheWeek.setListOfKDA(listOfKDA);
		playerDataOfTheWeek.setListOfSummonerSpellUsed(listOfSummonerSpellUsed);
		playerDataOfTheWeek.setListTotCreep10Minute(listTotCreep10Minute);
		playerDataOfTheWeek.setListTotCreep20Minute(listTotCreep20Minute);
		playerDataOfTheWeek.setListTotCreep30Minute(listTotCreep30Minute);
		playerDataOfTheWeek.setNbrGames(nbrGames);
		playerDataOfTheWeek.setNbrWin(nbrWin);
		
		return playerDataOfTheWeek;
	}
}
