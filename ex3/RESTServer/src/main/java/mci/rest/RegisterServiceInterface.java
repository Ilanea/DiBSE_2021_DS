package mci.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("register")
public interface RegisterServiceInterface {
    @GET
    @Path("/registerService")
    @Produces({ MediaType.APPLICATION_JSON })
    public String registerService(@QueryParam("serverUrl") String serverUrl);

    @DELETE
    @Path("/unregisterService")
    @Produces({ MediaType.APPLICATION_JSON })
    public String unregisterService(@QueryParam("serverUrl") String serverUrl);
}

