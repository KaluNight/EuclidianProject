package continuousDataCheck;

import java.util.ArrayList;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import com.merakianalytics.orianna.types.core.match.Match;
import com.merakianalytics.orianna.types.core.match.MatchHistory;
import com.merakianalytics.orianna.types.core.match.Participant;
import com.merakianalytics.orianna.types.core.searchable.SearchableList;
import com.merakianalytics.orianna.types.core.staticdata.Champion;
import com.merakianalytics.orianna.types.core.staticdata.SummonerSpell;
import com.merakianalytics.orianna.types.core.summoner.Summoner;

import main.Main;
import model.KDA;
import model.Player;
import model.PlayerDataOfTheWeek;

public class ContinuousKeepData extends TimerTask{

	private static int indexPlayerSelected = -1;
	
	private static DateTime weekDateStart; //TODO: Date need to be set Every week
	
	private static DateTime weekDateEnd; //TODO: Date need to be set Every week
	
	//TODO: Object for stack treated history of player
	
	
	/**
	 * Run this every end of week. (TODO)
	 * Get All player history of in the week and return a PlayerDataObject (TODO)
	 */
	@Override
	public void run() {
		for(int i = 0; i < Main.getPlayerList().size(); i++) {
			if((indexPlayerSelected + 1) >= Main.getPlayerList().size()) {
				indexPlayerSelected = 0;
			}else {
				indexPlayerSelected++;
			}
			
			Player player = Main.getPlayerList().get(indexPlayerSelected);
			Summoner summoner = player.getSummoner();
			
			MatchHistory matchHistory = MatchHistory.forSummoner(summoner).withStartTime(weekDateStart).withEndTime(weekDateEnd).get();
			
			PlayerDataOfTheWeek playerDataOfTheWeek = treatmentData(matchHistory, player.getSummoner());
			
			player.getListDataOfWeek().add(playerDataOfTheWeek);
			Main.getPlayerList().remove(indexPlayerSelected);
			Main.getPlayerList().add(player); //Check if copy
			
			generatingStats(player.getListDataOfWeek());
		}
	}
	
	private void generatingStats(ArrayList<PlayerDataOfTheWeek> listPlayesData) {
		XYChart chart = new XYChartBuilder()
				.width(600)
				.height(500)
				.title("Graphique Winrate")
				.xAxisTitle("Semaine")
				.yAxisTitle("Winrate Moyen")
				.build();
		//TODO: Suite
	}
	
	private PlayerDataOfTheWeek treatmentData(MatchHistory matchHistory, Summoner summoner) {
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
