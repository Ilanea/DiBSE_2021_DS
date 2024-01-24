package mci.rest;

public class FingerInterval {
    Integer startInterval;
    Integer endInterval;

    public FingerInterval(Integer start, Integer end) {
        this.startInterval = start;
        this.endInterval = end;
    }

    public String getInterval() {
        return "<" + this.startInterval + "," + this.endInterval + ">";
    }
    public void setInterval(Integer start, Integer end) {
        this.startInterval = start;
        this.endInterval = end;
    }
}
