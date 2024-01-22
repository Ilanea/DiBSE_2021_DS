package mci.rest;

import java.util.*;
import java.net.InetAddress;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VisualizeChordNetwork extends Application {


    final static int SERVER_PORT = 8000;
    final static String SERVER_URL = "http://localhost:" + SERVER_PORT;
    final static String API_ENDPOINT = "/api";
    static String startPort = null;
    static String endPort = null;
    static List<ChordNode> nodeList = new ArrayList<>();

    private Set<Object> singletons = new HashSet<Object>();
    static Logger log = LoggerFactory.getLogger(VisualizeChordNetwork.class);

    public VisualizeChordNetwork()
    {
        singletons.add(ChordNetworkInterface.class);
        singletons.add(new NodeResource());
    }

    @Override
    public Set<Object> getSingletons()
    {
        return singletons;
    }

    public static void main(String[] args) throws Exception {
        log.info("Starting VisualizeChordNetwork");

        // Starting Jetty Server in an extra Thread
        Thread serverThread = new Thread(() -> {
            try {
                startServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        Map<String, String> env = System.getenv();
        startPort = env.get("START_PORT");
        endPort = env.get("END_PORT");

        if (startPort == null || endPort == null) {
            log.error("START_PORT or END_PORT not set");
            System.exit(1);
        }

        for(int i = Integer.parseInt(startPort); i <= Integer.parseInt(endPort); i++) {
            log.info("Checking node " + i);

            String port = Integer.toString(i);
            String url = "http://localhost:" + port + "/api";
            String nodeInfo = null;

            try {
                nodeInfo = getNodeInfo(url);
            } catch (Exception e) {
                log.error("Node " + port + " not reachable");
                break;
            }

            if(nodeInfo == null) {
                log.error("Node " + port + " not reachable");;
            } else {
                JSONArray json = new JSONArray(nodeInfo);
                JSONObject nodeJson = json.getJSONObject(1);
                JSONArray fingerTableJson = json.getJSONArray(2);

                String address = nodeJson.getString("address");
                Integer id = nodeJson.getInt("id");
                Integer successor = nodeJson.getInt("successor");
                Integer predecessor = nodeJson.getInt("predecessor");

                // Assuming the structure of Finger class and its constructor
                Finger[] fingers = new Finger[fingerTableJson.length()];
                for (int j = 0; j < fingerTableJson.length(); j++) {
                    JSONObject fingerJson = fingerTableJson.getJSONObject(j);
                    String successorF = fingerJson.getString("successor");
                    Integer start = fingerJson.getInt("start");
                    fingers[j] = new Finger(start, successorF);
                }

                log.info("Add Node " + id + " to nodeList with address " + address + " and successor " + successor + " and predecessor " + predecessor);
                ChordNode node = new ChordNode(address, id, successor, predecessor, fingers);
                nodeList.add(node);
            }
        }
    }


    private static void startServer() throws Exception {

        Server server = new Server(SERVER_PORT);

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Set up RESTEasy servlet
        // Only works with Jetty version < 11
        final ServletHolder restEasyServlet = new ServletHolder("restEasyServlet", new HttpServletDispatcher());
        restEasyServlet.setInitParameter("resteasy.servlet.mapping.prefix", API_ENDPOINT);
        restEasyServlet.setInitParameter("javax.ws.rs.Application", VisualizeChordNetwork.class.getName());
        context.addServlet(restEasyServlet, API_ENDPOINT + "/*");

        // Set up default servlet to handle static content (optional)
        final ServletHolder defaultServlet = new ServletHolder("static", DefaultServlet.class);
        defaultServlet.setInitParameter("resourceBase", "./src/main/resources/static");
        defaultServlet.setInitParameter("dirAllowed", "true");
        context.addServlet(defaultServlet, "/*");

        server.setHandler(context);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getNodeInfo(String clientUrl) throws Exception {
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(clientUrl);
        ResteasyWebTarget rtarget = (ResteasyWebTarget)target;
        ChordNetworkInterface node = rtarget.proxy(ChordNetworkInterface.class);

        String nodeInfo;

        try{
            nodeInfo = node.getNodeInfo();
        } catch (Exception e) {
            log.error("Failed to fetch data from " + clientUrl + ": " + e.getMessage());
            return null;
        } finally {
            client.close();
        }

        return nodeInfo;
    }

}
