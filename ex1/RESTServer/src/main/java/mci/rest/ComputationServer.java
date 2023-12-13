package mci.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

@ApplicationPath("/api")
public class ComputationServer extends Application
{
    private Set<Object> singletons = new HashSet<Object>();

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
        Server server = new Server(8080);

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Set up RESTEasy servlet
        // Only works with Jetty version < 11
        final ServletHolder restEasyServlet = new ServletHolder("restEasyServlet", new HttpServletDispatcher());
        restEasyServlet.setInitParameter("resteasy.servlet.mapping.prefix", "/api");
        restEasyServlet.setInitParameter("javax.ws.rs.Application", ComputationServer.class.getName());
        context.addServlet(restEasyServlet, "/api/*");

        // Set up default servlet to handle static content (optional)
        final ServletHolder defaultServlet = new ServletHolder("static", DefaultServlet.class);
        defaultServlet.setInitParameter("resourceBase", "./src/main/webapp");
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
}