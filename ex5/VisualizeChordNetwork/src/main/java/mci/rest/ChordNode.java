package mci.rest;

public class ChordNode {

        String address;
        Integer id;
        Integer predecessor;
        Integer successor;

        Finger[] fingers;

        public ChordNode(String address, Integer id, Integer successor, Integer predecessor, Finger[] fingers) {
            this.address = address;
            this.id = id;
            this.successor = successor;
            this.predecessor = predecessor;
            this.fingers = fingers;
        }

        public String getAddress() {
            return address;
        }

        public Integer getId() {
            return id;
        }

        public Integer getSuccessor() {
            return successor;
        }

        public Integer getPredecessor() {
            return predecessor;
        }

        public Finger[] getFingers() {
            return fingers;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public void setFingers(Finger[] fingers) {
            this.fingers = fingers;
        }
}
