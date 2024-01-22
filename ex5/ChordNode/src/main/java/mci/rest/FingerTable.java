package mci.rest;

public class FingerTable {

    Finger[] fingers;

    public FingerTable(int tableSize) {
        fingers = new Finger[tableSize];
    }

    public Finger[] getFingers() {
        return fingers;
    }

    public void setFinger(int index, Finger finger) {
        if (index >= 0 && index < fingers.length) {
            fingers[index] = finger;
        }
    }

    public Finger getFinger(int index) {
        if (index >= 0 && index < fingers.length) {
            return fingers[index];
        }
        return null;
    }
}