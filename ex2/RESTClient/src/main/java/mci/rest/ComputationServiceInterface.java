package mci.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/computationservice")
public interface ComputationServiceInterface {
    @GET
    @Path("/calculate")
    @Produces(MediaType.APPLICATION_JSON)
    String calculate(@QueryParam("n1") int n1, @QueryParam("n2") int n2, @QueryParam("op") String op);
}
