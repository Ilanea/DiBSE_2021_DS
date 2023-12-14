package mci.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/register")
public class RegisterService {

    @GET
    @Path("/registerService")
    @Produces({ MediaType.APPLICATION_JSON })
    public String registerService(@QueryParam("serverUrl") String serverUrl, @Context HttpServletRequest req) {
        BackendManager mgr = BackendManager.getInstance();

        System.out.println(req.getRemoteAddr() + ":" + req.getRemotePort() + " wants to register service url " + serverUrl);

        // Add service url to backend service list
        if (serverUrl != null && !serverUrl.isEmpty() && mgr.addBackendService(serverUrl)) {
            System.out.println("Success");
            return "\"OK\"";
        } else {
            System.out.println("Error");
            return "\"ERROR\"";
        }
    }

    @DELETE
    @Path("/unregisterService")
    @Produces({ MediaType.APPLICATION_JSON })
    public String unregisterService(@QueryParam("serverUrl") String serverUrl, @Context HttpServletRequest req) {
        BackendManager mgr = BackendManager.getInstance();

        System.out.println(req.getRemoteAddr() + ":" + req.getRemotePort() + " wants to unregister service url " + serverUrl);

        // Remove service url from backend service list
        if (serverUrl != null && !serverUrl.isEmpty() && mgr.removeBackendService(serverUrl)) {
            System.out.println("Success");
            return "\"OK\"";
        } else {
            System.out.println("Error");
            return "\"ERROR\"";
        }
    }
}
