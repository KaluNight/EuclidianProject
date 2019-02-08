package ch.euclidian.main.model;

public class GraphData {

  private String messageString;
  private String graphName;
  private byte[] graphData;
  
  public GraphData(String messageString, String graphName, byte[] graphData) {
    this.messageString = messageString;
    this.graphName = graphName;
    this.graphData = graphData;
  }
  
  public String getMessageString() {
    return messageString;
  }

  public String getGraphName() {
    return graphName;
  }

  public byte[] getGraphData() {
    return graphData;
  }
  
}
