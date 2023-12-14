package mci.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/testservice")
public class TestService {
    @GET
    @Path("/helloworld")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloworld() {
        return "Hello World!";
    }
}
