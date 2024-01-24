package mci.rest;

public class Finger {

    Integer start;
    String nodeAddress;
    FingerInterval interval;

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

    public void setInterval(Integer start, Integer end) {
        this.interval = new FingerInterval(start, end);
    }
    public String getInterval() {
        return this.interval.getInterval();
    }
}