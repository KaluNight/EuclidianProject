package model;

public class KDA {
	private int kills;
	private int deaths;
	private int assists;
	
	public KDA(int kills, int deaths, int assists) {
		this.kills = kills;
		this.deaths = deaths;
		this.assists = assists;
	}

	public int getKills() {
		return kills;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}

	public int getDeaths() {
		return deaths;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	public int getAssists() {
		return assists;
	}

	public void setAssists(int assists) {
		this.assists = assists;
	}
}
