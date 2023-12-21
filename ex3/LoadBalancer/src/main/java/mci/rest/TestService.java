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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/testservice")
public class TestService {
    private static Random rand = new Random();
    static Logger log = LoggerFactory.getLogger(TestService.class);

    @GET
    @Path("/helloworld")
    @Produces({ MediaType.TEXT_PLAIN })
    public String getHelloMessage(@Context HttpServletRequest req) {

        log.info(req.getRemoteAddr() + ":" + req.getRemotePort() + " called getHelloMessage()");

        // Get list of backend servers and randomly select one
        BackendManager mgr = BackendManager.getInstance();
        List<String> serverList =mgr.getBackendServices();
        String restServiceUrl = serverList.get(rand.nextInt(serverList.size()));

        log.info("Redirecting request to " + restServiceUrl);

        // Create client
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(restServiceUrl);
        ResteasyWebTarget rtarget = (ResteasyWebTarget)target;

        // Create Service
        TestServiceInterface hello = rtarget.proxy(TestServiceInterface.class);

        String result = hello.getHelloMessage();
        log.info(result);

        return result;
    }
}
