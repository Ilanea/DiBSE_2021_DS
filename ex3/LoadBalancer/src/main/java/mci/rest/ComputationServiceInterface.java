package mci.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/computationservice")
public interface ComputationServiceInterface {
    @GET
    @Path("/calculate")
    @Produces({MediaType.APPLICATION_JSON})
    public String CalculateValue(@QueryParam("n1") String n1, @QueryParam("n2") String n2, @QueryParam("op") String op);
}

