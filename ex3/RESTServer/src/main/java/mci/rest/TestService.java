package mci.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/testservice")
public class TestService {
    static Logger log = LoggerFactory.getLogger(TestService.class.getName());

    @GET
    @Path("/helloworld")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloworld(@Context HttpServletRequest req) {
        log.info("Received request from " + req.getRemoteAddr() + " to say hello world");
        return "Hello World!";
    }
}
