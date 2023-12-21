package mci.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadBalancer extends Application
{
    final static int SERVER_PORT = 8888;
    final static String SERVER_URL = "http://0.0.0.0:" + SERVER_PORT;
    final static String API_ENDPOINT = "/api";
    private Set<Object> singletons = new HashSet<Object>();
    static Logger log = LoggerFactory.getLogger(LoadBalancer.class);

    public LoadBalancer()
    {
        singletons.add(new RegisterService());
        singletons.add(new TestService());
        singletons.add(new ComputationService());
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
        restEasyServlet.setInitParameter("javax.ws.rs.Application", LoadBalancer.class.getName());
        context.addServlet(restEasyServlet, API_ENDPOINT + "/*");

        // Set up default servlet to handle static content (optional)
        final ServletHolder defaultServlet = new ServletHolder("static", DefaultServlet.class);
        defaultServlet.setInitParameter("resourceBase", "./src/main/webapp");
        defaultServlet.setInitParameter("dirAllowed", "true");
        context.addServlet(defaultServlet, "/*");

        server.setHandler(context);

        log.info("Server started at " + SERVER_URL + API_ENDPOINT);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}