package mci.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/computationservice")
public class ComputationService {
    @GET
    @Path("/calculate")
    @Produces(MediaType.APPLICATION_JSON)
    public String add(@QueryParam("n1") int n1, @QueryParam("n2") int n2, @QueryParam("op") String op) {
        if(op.equals("add"))
            return String.valueOf(n1 + n2);
        else if(op.equals("sub"))
            return String.valueOf(n1 - n2);
        else if(op.equals("mul"))
            return String.valueOf(n1 * n2);
        else if(op.equals("div"))
            return String.valueOf(n1 / n2);
        else
            return "Invalid operation";
    }
}
