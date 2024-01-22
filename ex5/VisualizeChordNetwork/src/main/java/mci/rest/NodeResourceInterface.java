package mci.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/node-info")
public interface NodeResourceInterface {
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    List<ChordNode> getNodes();

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    String getHello();
}
