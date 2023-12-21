package mci.rest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComputationServer extends Application
{
    final static int SERVER_PORT = 8080;
    final static String SERVER_URL = "http://0.0.0.0:" + SERVER_PORT;
    final static String API_ENDPOINT = "/api";
    private Set<Object> singletons = new HashSet<Object>();
    static Logger log = LoggerFactory.getLogger(ComputationServer.class);

    public ComputationServer()
    {
        singletons.add(new ComputationService());
        singletons.add(new TestService());
    }

    @Override
    public Set<Object> getSingletons()
    {
        return singletons;
    }

    public static void main(String[] args) throws Exception {
        startServer();
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
        restEasyServlet.setInitParameter("javax.ws.rs.Application", ComputationServer.class.getName());
        context.addServlet(restEasyServlet, API_ENDPOINT + "/*");

        // Set up default servlet to handle static content (optional)
        final ServletHolder defaultServlet = new ServletHolder("static", DefaultServlet.class);
        defaultServlet.setInitParameter("resourceBase", "./src/main/webapp");
        defaultServlet.setInitParameter("dirAllowed", "true");
        context.addServlet(defaultServlet, "/*");

        server.setHandler(context);

        try {
            Map<String, String> env = System.getenv();
            String load_balancer_url =  env.get("LOAD_BALANCER_URL");
            InetAddress ip;
            log.info("Load Balancer URL: " + load_balancer_url);
            if(load_balancer_url != null && !load_balancer_url.isEmpty()) {
                ip = InetAddress.getLocalHost();
                String hostAddress = ip.getHostAddress();
                String serverUrl = "http://" + hostAddress + ":" + SERVER_PORT + API_ENDPOINT;

                // Create Client to Service
                Client client = ClientBuilder.newBuilder().build();
                WebTarget target = client.target(load_balancer_url);
                ResteasyWebTarget rtarget = (ResteasyWebTarget)target;

                RegisterServiceInterface proxy = rtarget.proxy(RegisterServiceInterface.class);
                String result = proxy.registerService(serverUrl);


                if (Objects.equals(result, "\"OK\"")) {
                    log.info("Service registered on Loadbalancer: " + load_balancer_url);
                    log.info("Service URL: " + serverUrl);
                } else {
                    log.info("Service could not be registered on Loadbalancer: " + load_balancer_url);
                }
            } else {
                log.error("No Load Balancer URL found. Starting server without registration.");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}