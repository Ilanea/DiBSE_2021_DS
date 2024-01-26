package mci.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.servlet.http.HttpServletRequest;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Path("/node")
public class ChordNode {
    private static int FINGERTABLE_SIZE;
    private Integer id;
    private String address;
    private FingerTable fingerTable;
    private Integer predecessorId;
    private String predecessorAddress;
    private Integer next = 0;
    private HashMap<Integer, String> data = new HashMap<Integer, String>();
    static Logger log = LoggerFactory.getLogger(ChordNode.class);

    public ChordNode(Integer fingertableSize) {
        this.FINGERTABLE_SIZE = fingertableSize;
        this.fingerTable = new FingerTable(FINGERTABLE_SIZE);
    }


    /*
     *
     * Getter and Setter
     *
     */

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getId() {
        return this.id;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getAddress() {
        return this.address;
    }
    public void setFingerTable(FingerTable fingerTable) {
        this.fingerTable = fingerTable;
    }
    public FingerTable getFingerTable() {
        return this.fingerTable;
    }
    public void setPredecessorId(Integer predecessor) {
        this.predecessorId = predecessor;
    }
    public Integer getPredecessorId() {
        return this.predecessorId;
    }
    public void setPredecessorAddress(String predecessorAddress) {
        this.predecessorAddress = predecessorAddress;
    }
    public String getPredecessorAddress() {
        return this.predecessorAddress;
    }

    /*
     *
     * Fingertable
     *
     */

    public void initializeFirstFingerTable() {
        for (int i = 0; i < FINGERTABLE_SIZE; i++) {
            int start = (this.id + (1 << i)) % (1 << FINGERTABLE_SIZE);
            int end = (this.id + (1 << (i + 1))) % (1 << FINGERTABLE_SIZE) - 1;
            Finger finger = new Finger(start, this.address);
            finger.setInterval(start,end);
            this.fingerTable.setFinger(i, finger);
        }
    }

    public synchronized void initializeFingerTable() {
        log.info("Initializing finger table");
        for (int i = 0; i < FINGERTABLE_SIZE; i++) {
            int start = (this.id + (1 << i)) % (1 << FINGERTABLE_SIZE);
            int end = (this.id + (1 << (i + 1))) % (1 << FINGERTABLE_SIZE) - 1;

            JSONObject successor = new JSONObject(findSuccessor(start));
            String successorUrl = successor.getString("successorAddress");

            log.info("Initializing finger " + i + " with start: " + start + ", successor: " + successorUrl);

            Finger finger = new Finger(start, successorUrl);
            finger.setInterval(start,end);
            this.fingerTable.setFinger(i, finger);
        }
    }

    public void setSuccessor(String successorAddress) {
        int start = (this.id + (1 << 0)) % (1 << FINGERTABLE_SIZE);
        int end = (this.id + (1 << 1)) % (1 << FINGERTABLE_SIZE) - 1;
        Finger finger = new Finger(start, successorAddress);
        finger.setInterval(start,end);
        this.fingerTable.setFinger(0, finger);
    }

    public String getSuccessorAddress() {
        return this.fingerTable.getFinger(0).getNode();
    }

    public int getSuccessorId() {
        String successorAddress = this.fingerTable.getFinger(0).getNode();

        return chordId(successorAddress);
    }

    private boolean isBetween(int targetId, int startId, int endId, boolean inclusiveStart, boolean inclusiveEnd) {
        if (startId < endId) {
            // Normal interval
            if (inclusiveStart && inclusiveEnd) {
                return targetId >= startId && targetId <= endId;
            } else if (inclusiveStart) {
                return targetId >= startId && targetId < endId;
            } else if (inclusiveEnd) {
                return targetId > startId && targetId <= endId;
            } else {
                return targetId > startId && targetId < endId;
            }
        } else if (startId > endId) {
            // Interval wraps around the ring
            if (inclusiveStart && inclusiveEnd) {
                return targetId >= startId || targetId <= endId;
            } else if (inclusiveStart) {
                return targetId >= startId || targetId < endId;
            } else if (inclusiveEnd) {
                return targetId > startId || targetId <= endId;
            } else {
                return targetId > startId || targetId < endId;
            }
        } else {
            // startId == endId
            if (inclusiveStart && inclusiveEnd) {
                return targetId == startId;
            } else {
                return false;
            }
        }
    }


    private String findSuccessor(int nodeIdToFind) {
        log.info("Finding successor for node ID: " + nodeIdToFind);
        log.info("Current node ID: " + this.id + ", Successor ID: " + this.getSuccessorId());

        // If the provided node ID is between this node and its successor, return our successor (if it's available)
        if (isNodeReachable(this.getSuccessorAddress()) && (isBetween(nodeIdToFind, this.id, this.getSuccessorId(), false, true))) {
            log.info("Our current Successor is the successor of the Node ID: " + nodeIdToFind);
            return createSuccessorResponse(this.getSuccessorId(), this.getSuccessorAddress());
        }

        // Check finger table for a closer node
        else {
            // Find the closest preceding node from the finger table
            String closestPrecedingNodeAddress = findClosestPrecedingSuccessorNode(nodeIdToFind);

            if (!closestPrecedingNodeAddress.equals(this.address)) {
                // Forward the query to the closest preceding node
                return forwardFindSuccessorQuery(nodeIdToFind, closestPrecedingNodeAddress);
            } else {
                // If the closest preceding node is this node, return this node's successor
                return createSuccessorResponse(this.getSuccessorId(), this.getSuccessorAddress());
            }
        }
    }

    private String findClosestPrecedingSuccessorNode(int nodeIdToFind) {
        for (int i = FINGERTABLE_SIZE - 1; i >= 0; i--) {
            int fingerId = chordId(this.fingerTable.getFinger(i).getNode());
            if (isNodeReachable(this.fingerTable.getFinger(i).getNode())) {
                if (isBetween(fingerId, this.id, nodeIdToFind, false, false)) {
                    return this.fingerTable.getFinger(i).getNode();
                }
            }
        }
        return this.address;
    }

    private String createSuccessorResponse(int successorId, String successorAddress) {
        JSONObject response = new JSONObject();
        response.put("successorId", successorId);
        response.put("successorAddress", successorAddress);
        return response.toString();
    }

    private String forwardFindSuccessorQuery(int nodeIdToFind, String nodeAddress) {
        log.info("Forwarding findSuccessor query to find Node " + nodeIdToFind + " to " + nodeAddress);
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(nodeAddress);
        ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
        ChordNodeInterface node = rtarget.proxy(ChordNodeInterface.class);

        String successor = node.findSuccessorQuery(nodeIdToFind);

        return successor;
    }

    private String findPredecessor(int nodeIdToFind) {
        log.info("Finding predecessor for node ID: " + nodeIdToFind);
        log.info("Current node ID: " + this.id + ", Predecessor ID: " + this.predecessorId);

        // Check if the node itself is the predecessor
        if (nodeIdToFind == this.getSuccessorId()) {
            log.info("We are the predecessor, returning own address");
            return createPredecessorResponse(this.id, this.address);
        }

        // Check if we are the node to find
        else if (nodeIdToFind == this.id) {
            log.info("We are the node to find, returning own predecessor address");
            if(isNodeReachable(this.predecessorAddress)){
                return createPredecessorResponse(this.predecessorId, this.predecessorAddress);
            } else {
                return createPredecessorResponse(this.id, this.address);
            }
        }

        // Check finger table for a closer node
        else {
            // Find the closest preceding node from the finger table
            String closestPrecedingNodeAddress = findClosestPrecedingPredecessorNode(nodeIdToFind);

            if (!closestPrecedingNodeAddress.equals(this.address)) {
                // Forward the query to the closest preceding node
                return forwardFindPredecessorQuery(nodeIdToFind, closestPrecedingNodeAddress);
            } else {
                // If the closest preceding node is this node, return this node
                return createPredecessorResponse(this.id, this.address);
            }
        }
    }

    private String findClosestPrecedingPredecessorNode(int nodeIdToFind) {
        for (int i = FINGERTABLE_SIZE - 1; i >= 0; i--) {
            int fingerId = chordId(this.fingerTable.getFinger(i).getNode());
            if (isNodeReachable(this.fingerTable.getFinger(i).getNode())) {
                if (isBetween(nodeIdToFind, fingerId, this.id, false, true)) {
                    return this.fingerTable.getFinger(i).getNode();
                }
            }
        }
        return this.address;
    }

    private String createPredecessorResponse(int predecessorId, String predecessorAddress) {
        JSONObject response = new JSONObject();
        response.put("predecessorId", predecessorId);
        response.put("predecessorAddress", predecessorAddress);
        return response.toString();
    }

    private String forwardFindPredecessorQuery(int nodeIdToFind, String nodeAddress) {
        log.info("Forwarding findPredecessor query to find Node " + nodeIdToFind + " to " + nodeAddress);
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(nodeAddress);
        ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
        ChordNodeInterface node = rtarget.proxy(ChordNodeInterface.class);

        String predecessor = node.findPredecessorQuery(nodeIdToFind);

        return predecessor;
    }

    public String newNodeToFingerTable(int newNodeId, String newNodeAddress) {
        boolean updatedFinger = false;

        for (int i = 0; i < FINGERTABLE_SIZE; i++) {
            int start = (this.id + (1 << i)) % (1 << FINGERTABLE_SIZE);
            int end = (this.id + (1 << (i + 1))) % (1 << FINGERTABLE_SIZE) - 1;

            Finger currentFinger = this.fingerTable.getFinger(i);
            int currentFingerNodeId = chordId(currentFinger.getNode());

            //log.info("New node ID: " + newNodeId + ", current finger ID: " + currentFingerNodeId + ", start: " + start);
            //log.info("Is new node closer to start than current finger? " + isCloser(newNodeId, currentFingerNodeId, start));

            // Update finger if new node is closer to the start than the current finger's node
            if (isCloser(newNodeId, currentFingerNodeId, start)) {
                Finger finger = new Finger(start, newNodeAddress);
                finger.setInterval(start,end);
                this.fingerTable.setFinger(i, finger);
                log.warn("Updated finger table entry " + i + " with start: " + start + ", new node: " + newNodeAddress);
                updatedFinger = true;
            }
        }

        //this.initializeFingerTable();

        return updatedFinger ? "Finger table updated" : "Finger table not updated";
    }

    private boolean isCloser(int nodeId, int comparedNodeId, int targetId) {
        //log.info("Checking if node " + nodeId + " is closer to " + targetId + " than node " + comparedNodeId);

        int totalNodes = 1 << FINGERTABLE_SIZE;

        int distanceToTarget = (nodeId - targetId + totalNodes) % totalNodes;
        int comparedDistanceToTarget = (comparedNodeId - targetId + totalNodes) % totalNodes;

        //log.info("Distance to target: " + distanceToTarget + ", compared distance to target: " + comparedDistanceToTarget);
        //log.info(String.valueOf(distanceToTarget < comparedDistanceToTarget));
        return distanceToTarget < comparedDistanceToTarget;
    }

    private int chordId(String nodeAddress) {
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(nodeAddress);
        ResteasyWebTarget rtarget = (ResteasyWebTarget)target;
        ChordNodeInterface node = rtarget.proxy(ChordNodeInterface.class);

        String nodeInfo = node.getChordNodeId();
        JSONObject json = new JSONObject(nodeInfo);

        return json.getInt("id");
    }

    /*
    *
    * Message Handling
    *
    *
     */

    public String sendMessageToNode(int destinationId, String message) {
        if (this.id.equals(destinationId)) {
            log.info("Message received: " + message);

            return "Message delivered to node " + this.id;
        } else {
            // Forward the message to the appropriate node
            String nextNodeAddress = findNextNodeForMessage(destinationId);
            return forwardMessage(message, destinationId, nextNodeAddress);
        }
    }

    private String findNextNodeForMessage(int destinationId) {
        for (int i = FINGERTABLE_SIZE - 1; i >= 0; i--) {
            int fingerId = chordId(this.fingerTable.getFinger(i).getNode());
            if (fingerId != this.id && isBetween(fingerId, this.id, destinationId, false, false)) {
                return this.fingerTable.getFinger(i).getNode();
            }
        }
        return this.getSuccessorAddress();
    }

    private String forwardMessage(String message, int destinationId, String nextNodeAddress) {
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(nextNodeAddress);
        ResteasyWebTarget rtarget = (ResteasyWebTarget)target;
        ChordNodeInterface node = rtarget.proxy(ChordNodeInterface.class);

        return node.sendMessageToNode(destinationId, message);
    }

    /*
     *
     * Data Handling
     *
     *
     */

    public String addData(String value) {
        int key = Math.abs(value.hashCode()) % (1 << FINGERTABLE_SIZE);
        if(isBetween(key, this.predecessorId, this.id, false, true)){
            if(!this.data.containsKey(key)){
                this.data.put(key, value);
                return "Data with key " + key + " added to node " + this.id;
            } else {
                this.data.put(key, value);
                return "Data with key " + key + " already exists in node " + this.id + " overwriting value";
            }
        } else {
            String nextNodeAddress = findNextNodeForData(key);
            return forwardAddData(value, nextNodeAddress);
        }
    }

    public String removeData(Integer key){
        if(this.data.containsKey(key)){
            this.data.remove(key);
            return "Data with key " + key + " removed from node " + this.id;
        } else {
            String nextNodeAddress = findNextNodeForData(key);
            return forwardRemoveData(key, nextNodeAddress);
        }
    }

    public String getData(Integer key){
        if(this.data.containsKey(key)){
            return this.data.get(key);
        } else {
            String nextNodeAddress = findNextNodeForData(key);
            return forwardGetData(key, nextNodeAddress);
        }
    }

    private String findNextNodeForData(int key) {
        for (int i = FINGERTABLE_SIZE - 1; i >= 0; i--) {
            int fingerId = chordId(this.fingerTable.getFinger(i).getNode());
            if (fingerId != this.id && isBetween(fingerId, this.id, key, false, false)) {
                return this.fingerTable.getFinger(i).getNode();
            }
        }
        return this.getSuccessorAddress();
    }

    private String forwardAddData(String value, String nextNodeAddress) {
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(nextNodeAddress);
        ResteasyWebTarget rtarget = (ResteasyWebTarget)target;
        ChordNodeInterface node = rtarget.proxy(ChordNodeInterface.class);

        return node.addDataQuery(value);
    }

    private String forwardRemoveData(Integer key, String nextNodeAddress) {
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(nextNodeAddress);
        ResteasyWebTarget rtarget = (ResteasyWebTarget)target;
        ChordNodeInterface node = rtarget.proxy(ChordNodeInterface.class);

        return node.removeDataQuery(key);
    }

    private String forwardGetData(Integer key, String nextNodeAddress) {
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(nextNodeAddress);
        ResteasyWebTarget rtarget = (ResteasyWebTarget)target;
        ChordNodeInterface node = rtarget.proxy(ChordNodeInterface.class);

        return node.getDataQuery(key);
    }


    /*
    *
    *  Schedules Tasks
    *
    *
     */

    public synchronized void stabilize() {
        log.info("Running stabilize task on Node " + this.id);

        // Check if successor is myself
        if(this.address.equals(this.getSuccessorAddress())){
            log.info("Successor is myself. No need to stabilize.");
            return;
        }

        // Check if successor is reachable
        if (!isNodeReachable(this.getSuccessorAddress())) {
            log.error("Successor is not reachable. Need to find next available successor.");

            // We need to take the node out of our finger table and update the successor to the next node in the chord network
            // We also need to notify other nodes about this
            for (int i = 1; i < FINGERTABLE_SIZE; i++) {
                Finger finger = this.fingerTable.getFinger(i);
                if (isNodeReachable(finger.getNode())) {
                    // Update the successor with the first reachable node
                    this.setSuccessor(finger.getNode());
                    log.info("Updated successor to " + finger.getNode());

                    break;
                }
            }
        } else {
            log.info("Successor is reachable. Checking if successor's predecessor is this node.");

            String successorPredecessorAddress = getSuccessorsPredecessorAddress();

            // shit hack to check for empty json
            if(!successorPredecessorAddress.equals("{}")) {
                JSONObject json = new JSONObject(successorPredecessorAddress);

                int predecessorId = json.getInt("predecessorId");
                String predecessorAddress = json.getString("predecessorAddress");

                // If the successor's predecessor is not this node
                log.info("Successor's predecessor ID: " + predecessorId + ", this node ID: " + this.id);
                if (!predecessorAddress.isEmpty() && !predecessorAddress.equals(this.address)) {
                    if (isBetween(predecessorId, this.id, this.getSuccessorId(), false, true)) {
                        // Update successor if a closer node is found
                        this.setSuccessor(predecessorAddress);

                        log.info("Updated successor to " + this.getSuccessorAddress() + " with ID " + this.getSuccessorId());
                        this.newNodeToFingerTable(predecessorId, predecessorAddress);

                    }
                }
            }
        }

        if(isNodeReachable(this.getSuccessorAddress())) {
            // Notify successor that we are its predecessor
            log.info("Notify successor " + this.getSuccessorAddress() + " that we are its predecessor");
            this.notifySuccessor();
        } else {
            // Unlucky timing
            log.error("Successor is not reachable. Cannot notify successor.");
        }

        log.info("Stabilization task completed on Node " + this.id);
    }

    private synchronized void fixFingers(){
        log.info("Running fix fingers task on Node " + this.id);

        this.next = this.next + 1;
        if (this.next >= FINGERTABLE_SIZE) {
            next = 0;
        }

        int start = (this.id + (1 << next)) % (1 << FINGERTABLE_SIZE);
        int end = (this.id + (1 << (next + 1))) % (1 << FINGERTABLE_SIZE) - 1;

        log.info("Fixing finger " + next + " with ID " + start);
        JSONObject newFingerJSON = new JSONObject(findSuccessorQuery(start));
        Finger newFinger = new Finger(start, newFingerJSON.getString("successorAddress"));
        newFinger.setInterval(start, end);

        this.fingerTable.setFinger(next,newFinger);

        log.info("Fix fingers task completed on Node " + this.id);
    }

    private synchronized void checkPredecessor() {
        log.info("Running check predecessor task on Node " + this.id);

        if(this.predecessorId != null && this.predecessorAddress != null) {
            if (!isNodeReachable(this.predecessorAddress)) {
                log.warn("Predecessor is not reachable. Set predecessor to NULL.");
                this.predecessorId = null;
                this.predecessorAddress = null;
            }
        } else {
            log.warn("Predecessor is NULL. Waiting to get a new predecessor.");
        }

        log.info("Check predecessor task completed on Node " + this.id);
    }

    private String getSuccessorsPredecessorAddress() {
        try {
            Client client = ClientBuilder.newBuilder().build();
            WebTarget target = client.target(this.getSuccessorAddress());
            ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
            ChordNodeInterface node = rtarget.proxy(ChordNodeInterface.class);

            String successorPredecessorAddress = node.getPredecessor();

            return successorPredecessorAddress;
        } catch (Exception e) {
            log.error("Error during getSuccessorsPredecessorAddress: " + e.getMessage());
            return "";
        }


    }

    private void notifySuccessor() {
        try {
            Client client =  ClientBuilder.newBuilder().build();
            WebTarget target = client.target(this.getSuccessorAddress());
            ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
            ChordNodeInterface node = rtarget.proxy(ChordNodeInterface.class);

            node.updatePredecessorQuery(this.id, this.address);
        } catch (Exception e) {
            log.error("Error during notifySuccessor: " + e.getMessage());
        }
    }

    private boolean isNodeReachable(String nodeAddress) {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(5, TimeUnit.SECONDS);
        clientBuilder.readTimeout(5, TimeUnit.SECONDS);

        Client client = clientBuilder.build();
        WebTarget target = client.target(nodeAddress);
        ResteasyWebTarget rtarget = (ResteasyWebTarget) target;
        ChordNodeInterface node = rtarget.proxy(ChordNodeInterface.class);

        try {
            if(node.ping().equals("OK")) {
                //log.info("Node " + nodeAddress + " is reachable");
                return true;
            } else {
                log.error("Node " + nodeAddress + " is not reachable");
                return false;
            }
        } catch (Exception e) {
            log.error("Error during isNodeReachable: " + e.getMessage());
            return false;
        } finally {
            client.close();
        }
    }

    // Method to schedule periodic tasks
    public void schedulePeriodicTasks() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            stabilize();
            fixFingers();
            checkPredecessor();
        }, 10, 10, TimeUnit.SECONDS);
    }


    /*
     *
     * API Endpoints
     *
     */

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getChordNode(@Context HttpServletRequest req) {
        log.info("Received request from " + req.getRemoteAddr() + " to get chord node info");

        JSONArray chord = new JSONArray();
        chord.put("Chord Node Info");

        JSONObject info = new JSONObject();
        info.put("id", this.id);
        info.put("address", this.address);
        info.put("successor", this.getSuccessorId());
        info.put("successorAddress", this.getSuccessorAddress());
        info.put("predecessor", this.predecessorId);
        info.put("predecessorAddress", this.predecessorAddress);

        chord.put(info);

        if(this.fingerTable != null) {
            JSONArray fingerTable = new JSONArray();
            for (int i = 0; i < FINGERTABLE_SIZE; i++) {
                JSONObject finger = new JSONObject();
                finger.put("start", this.fingerTable.getFinger(i).getStart());
                finger.put("interval", this.fingerTable.getFinger(i).getInterval());
                finger.put("address", this.fingerTable.getFinger(i).getNode());
                fingerTable.put(finger);
            }
            chord.put(fingerTable);
        }

        if(!this.data.isEmpty()){
            JSONArray data = new JSONArray();
            for (int key : this.data.keySet()) {
                JSONObject entry = new JSONObject();
                entry.put("key", key);
                entry.put("value", this.data.get(key));
                data.put(entry);
            }
            chord.put(data);
        }

        return chord.toString();
    }

    @POST
    @Path("/join")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized String joinChordNetwork(@Context HttpServletRequest req, @QueryParam("id") int id, @QueryParam("address") String address) {
        log.info("Received request from " + req.getRemoteAddr() + " to join the chord network with id " + id + " and address " + address);

        // Find the successor & predecessor for the new node
        JSONObject successorForNewNode = new JSONObject(this.findSuccessor(id));
        JSONObject predecessorForNewNode = new JSONObject(this.findPredecessor(id));

        // Create a JSON response with successor and predecessor info
        int successorId = successorForNewNode.getInt("successorId");
        int predecessorId = predecessorForNewNode.getInt("predecessorId");
        String successorAddress = successorForNewNode.getString("successorAddress");
        String predecessorAddress = predecessorForNewNode.getString("predecessorAddress");

        JSONObject response = new JSONObject();
        response.put("successorId", successorId);
        response.put("successorAddress", successorAddress);
        response.put("predecessorId", predecessorId);
        response.put("predecessorAddress", predecessorAddress);

        // add the new node to my fingertable
        this.newNodeToFingerTable(id, address);

        return response.toString();
    }

    @GET
    @Path("/id")
    @Produces(MediaType.APPLICATION_JSON)
    public String getChordNodeId() {
        JSONObject response = new JSONObject();
        response.put("id", this.id);

        return response.toString();
    }

    @GET
    @Path("/address")
    @Produces(MediaType.APPLICATION_JSON)
    public String getChordNodeAddress() {
        JSONObject response = new JSONObject();
        response.put("address", this.address);

        return response.toString();
    }

    @GET
    @Path("/find-successor/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String findSuccessorQuery(@PathParam("id") int id) {
        log.info("Received request to find successor of node:" + id);

        String successor = findSuccessor(id);

        log.info("Returning successor " + successor);
        return successor;
    }

    @POST
    @Path("/update-successor")
    @Consumes(MediaType.APPLICATION_JSON)
    public String updateSuccessorQuery(@QueryParam("id") int successorId, @QueryParam("address") String successorAddress) {
        log.info("Received request to update successor to Node " + successorId + " with Address " + successorAddress);

        if(isBetween(successorId, this.id, this.getSuccessorId(), true, true)){
            log.info("New successor is closer to us than our current successor. Updating successor.");
            this.setSuccessor(successorAddress);

            //this.newNodeToFingerTable(successorId, successorAddress);
            this.initializeFingerTable();

            return "Successor updated";
        } else {
            log.info("New successor is not closer to us than our current successor. Not updating successor.");
            return "Successor not updated";
        }
    }

    @GET
    @Path("/find-predecessor/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String findPredecessorQuery(@PathParam("id") int id) {
        log.info("Received request to find predecessor of node: " + id);

        String predecessor = findPredecessor(id);

        log.info("Returning predecessor " + predecessor);
        return predecessor;
    }

    @GET
    @Path("/get-predecessor")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPredecessorQuery() {
        log.info("Received request to get predecessor");

        JSONObject response = new JSONObject();
        response.put("predecessorId", this.predecessorId);
        response.put("predecessorAddress", this.predecessorAddress);

        return response.toString();
    }

    @POST
    @Path("/update-predecessor")
    @Consumes(MediaType.APPLICATION_JSON)
    public String updatePredecessorQuery(@QueryParam("id") int predecessorId, @QueryParam("address") String predecessorAddress) {
        log.info("Received request to update our current predecessor to Node " + predecessorId + " with Address " + predecessorAddress);

        if(this.predecessorId == null && this.predecessorAddress == null){
            log.info("We don't have a predecessor yet. Updating predecessor.");
            this.setPredecessorAddress(predecessorAddress);
            this.setPredecessorId(predecessorId);

            return "Predecessor updated";
        } else if(this.predecessorId == predecessorId){
            log.info("New predecessor is the same as our current predecessor. Not updating predecessor.");
            return "Predecessor not updated";
        } else if(isCloser(predecessorId, this.id, this.predecessorId)){
            log.info("New predecessor is closer to us than our current predecessor. Updating predecessor.");
            this.setPredecessorAddress(predecessorAddress);
            this.setPredecessorId(predecessorId);

            return "Predecessor updated";
        } else {
            log.info("New predecessor is not closer to us than our current predecessor. Not updating predecessor.");
            return "Predecessor not updated";
        }
    }

    @POST
    @Path("/update-finger-table")
    @Consumes(MediaType.APPLICATION_JSON)
    public String updateFingerTable(@QueryParam("id") int newNodeId, @QueryParam("address") String newNodeAddress){
        log.info("Received request to update finger table with new node " + newNodeId + " with address " + newNodeAddress);

        // Add a new node to the finger table
        return this.newNodeToFingerTable(newNodeId, newNodeAddress);
    }

    @GET
    @Path("/health-check")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "OK";
    }

    @GET
    @Path("/leave")
    @Produces(MediaType.TEXT_PLAIN)
    public void leave() {
        log.info("Received request to leave the network");

        // Create Client for Successor
        Client successorClient = ClientBuilder.newBuilder().build();
        WebTarget successorTarget = successorClient.target(this.getSuccessorAddress());
        ResteasyWebTarget rSuccessorTarget = (ResteasyWebTarget)successorTarget;
        ChordNodeInterface successor = rSuccessorTarget.proxy(ChordNodeInterface.class);


        // Create Client for Predecessor
        Client predecessorClient = ClientBuilder.newBuilder().build();
        WebTarget predecessorTarget = predecessorClient.target(this.getPredecessorAddress());
        ResteasyWebTarget rPredecessorTarget = (ResteasyWebTarget)predecessorTarget;
        ChordNodeInterface predecessor = rPredecessorTarget.proxy(ChordNodeInterface.class);

        if(isNodeReachable(this.getSuccessorAddress()) && isNodeReachable(this.getPredecessorAddress())) {
            successor.updatePredecessorQuery(this.getPredecessorId(), this.getPredecessorAddress());
            predecessor.updateSuccessorQuery(this.getSuccessorId(), this.getSuccessorAddress());

            System.exit(0);
        } else {
            log.error("Successor or Predecessor is not reachable, still leaving.");
            System.exit(0);
        }
    }

    @POST
    @Path("/send-message")
    @Consumes(MediaType.APPLICATION_JSON)
    public String sendMessage(@QueryParam("destinationId") int destinationId, @QueryParam("message") String message) {
        log.info("Received request to send message to node " + destinationId + " with message " + message);

        return this.sendMessageToNode(destinationId, message);
    }

    @PUT
    @Path("/data/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public String addDataQuery(@QueryParam("value") String value){
        return this.addData(value);
    }

    @DELETE
    @Path("/data/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    public String removeDataQuery(@QueryParam("key") Integer key){
        return this.removeData(key);
    }

    @GET
    @Path("/data/get")
    @Consumes(MediaType.APPLICATION_JSON)
    public String getDataQuery(@QueryParam("key") Integer key){
        return this.getData(key);
    }

}
