package mci.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import java.util.*;

@Path("/testservice")
public class TestService {
    private static Random rand = new Random();

    @GET
    @Path("/helloworld")
    @Produces({ MediaType.TEXT_PLAIN })
    public String getHelloMessage(@Context HttpServletRequest req) {

        System.out.println(req.getRemoteAddr() + ":" + req.getRemotePort() + " called getHelloMessage()");

        // Get list of backend servers and randomly select one
        BackendManager mgr = BackendManager.getInstance();
        List<String> serverList =mgr.getBackendServices();
        String restServiceUrl = serverList.get(rand.nextInt(serverList.size()));

        System.out.println("Redirecting request to " + restServiceUrl);

        // Create client
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(restServiceUrl);
        ResteasyWebTarget rtarget = (ResteasyWebTarget)target;

        // Create Service
        TestServiceInterface hello = rtarget.proxy(TestServiceInterface.class);

        String result = hello.getHelloMessage();
        System.out.println(result);

        return result;
    }
}
