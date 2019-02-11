package ch.euclidian.main.model;

import org.joda.time.DateTime;

public class DatedFullTier {

  private FullTier fullTier;
  private String creationTime; //ISO 8601

  public DatedFullTier(FullTier fullTier) {
    this.fullTier = fullTier;
    creationTime = DateTime.now().toString();
  }

  public FullTier getFullTier() {
    return fullTier;
  }
  
  public String getCreationTimeString() {
    return creationTime;
  }
  
  public DateTime getCreationTime() {
	  return DateTime.parse(creationTime);
  }
}
