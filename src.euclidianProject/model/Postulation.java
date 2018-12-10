package model;

import java.util.ArrayList;

import com.merakianalytics.orianna.types.core.summoner.Summoner;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

public class Postulation {
	
	private Member member;
	private Summoner summoner;
	private ArrayList<Role> roles;
	private String horaires;
	
	public Postulation(Member member, Summoner summoner, ArrayList<Role> roles, String horaires) {
		this.member = member;
		this.summoner = summoner;
		this.roles = roles;
		this.horaires = horaires;
	}
	
	@Override
	public String toString() {
		String str = "Postulation :\n";
		str += "Mon pseudo : " + summoner.getName() + "\n";
		str += "Mes rôles : ";
		
		for(int i = 0; i < roles.size(); i++) {
			str += roles.get(i).getName();
			if(!((i + 1) == roles.size())) {
				str += ", ";
			}
		}
		str += "\n";
		
		str += "Mes horaires : " + horaires;
		return str;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Summoner getSummoner() {
		return summoner;
	}

	public void setSummoner(Summoner summoner) {
		this.summoner = summoner;
	}

	public ArrayList<Role> getRoles() {
		return roles;
	}

	public void setRoles(ArrayList<Role> roles) {
		this.roles = roles;
	}

	public String getHoraires() {
		return horaires;
	}

	public void setHoraires(String horaires) {
		this.horaires = horaires;
	}
	
}
