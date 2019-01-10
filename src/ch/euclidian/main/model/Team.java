package ch.euclidian.main.model;

import java.util.ArrayList;
import java.util.Timer;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Role;

public class Team {

  private static ArrayList<Permission> permissionsList;

  private String name;
  private ArrayList<Player> players;
  private Timer timer;
  private Category category;
  private Role role;

  public Team(String name, ArrayList<Player> players) {
    this.name = name;
    this.players = players;
  }

  public Team(String name, Category category, Role role) {
    this.name = name;
    this.category = category;
    this.role = role;
    players = new ArrayList<Player>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ArrayList<Player> getPlayers() {
    return players;
  }

  public void setPlayers(ArrayList<Player> players) {
    this.players = players;
  }

  public Timer getTimer() {
    return timer;
  }

  public void setTimer(Timer timer) {
    this.timer = timer;
  }

  public static ArrayList<Permission> getPermissionsList() {
    return permissionsList;
  }

  public static void setPermissionsList(ArrayList<Permission> permissionsList) {
    Team.permissionsList = permissionsList;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

}