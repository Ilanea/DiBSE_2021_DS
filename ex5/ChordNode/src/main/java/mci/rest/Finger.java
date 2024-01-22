package mci.rest;

public class Finger {

    Integer start;

    String nodeAddress;

    public Finger(Integer start, String node) {
        this.start = start;
        this.nodeAddress = node;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public String getNode() {
        return nodeAddress;
    }

    public void setNode(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

}