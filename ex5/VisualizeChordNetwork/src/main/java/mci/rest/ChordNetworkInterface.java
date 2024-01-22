package mci.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/node")
public interface ChordNetworkInterface {

    @GET
    @Path("/")
    String getNodeInfo();
}
