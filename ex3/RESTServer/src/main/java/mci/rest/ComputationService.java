package mci.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/computationservice")
public class ComputationService {
    static Logger log = LoggerFactory.getLogger(ComputationService.class.getName());

    @GET
    @Path("/calculate")
    @Produces(MediaType.APPLICATION_JSON)
    public String add(@QueryParam("n1") int n1, @QueryParam("n2") int n2, @QueryParam("op") String op, @Context HttpServletRequest req) {

        log.info("Received request from " + req.getRemoteAddr() + " to calculate " + n1 + " " + op + " " + n2);

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
