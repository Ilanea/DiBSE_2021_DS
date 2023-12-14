package mci.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/testservice")
public interface TestServiceInterface {

    @GET
    @Path("/helloworld")
    @Produces(MediaType.TEXT_PLAIN)
    String getHelloMessage();
}
