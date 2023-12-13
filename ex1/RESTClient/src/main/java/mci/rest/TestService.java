package mci.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/testservice")
public interface TestService {

    @GET
    @Path("/helloworld")
    @Produces(MediaType.TEXT_PLAIN)
    String getHelloMessage();
}
