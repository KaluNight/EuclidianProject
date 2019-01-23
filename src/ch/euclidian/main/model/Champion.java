package ch.euclidian.main.model;

import java.io.File;

public class Champion {
  private int key;
  private String id;
  private String name;
  private File championLogo;

  public Champion(final int key, final String id, final String name, File championLogo) {
    this.id = id;
    this.key = key;
    this.name = name;
  }

  public int getKey() {
    return this.key;
  }

  public void setKey(final int key) {
    this.key = key;
  }

  public String getId() {
    return this.id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public File getChampionLogo() {
    return championLogo;
  }

  public void setChampionLogo(File championLogo) {
    this.championLogo = championLogo;
  }
}
