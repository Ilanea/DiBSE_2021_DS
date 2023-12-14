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

@Path("/computationservice")
public class ComputationService {
    private static Random rand = new Random();

    @GET
    @Path("/calculate")
    @Produces({ MediaType.APPLICATION_JSON })
    public String CalculateValue(@QueryParam("n1") String n1,@QueryParam("n2") String n2,@QueryParam("op") String op, @Context HttpServletRequest req) {

        System.out.println(req.getRemoteAddr() + ":" + req.getRemotePort() + " called CalculateValue(" + n1 + ", " + n2 + ", " + op + ")");

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
        ComputationServiceInterface calc = rtarget.proxy(ComputationServiceInterface.class);

        String result = calc.CalculateValue(n1,n2, op);
        System.out.println("Returning " + result);

        return result;
    }
}
