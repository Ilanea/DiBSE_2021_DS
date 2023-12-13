package mci.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/computationservice")
public interface ComputationService {
    @GET
    @Path("/calculate")
    @Produces(MediaType.APPLICATION_JSON)
    String calculate(@QueryParam("n1") int n1, @QueryParam("n2") int n2, @QueryParam("op") String op);
}
