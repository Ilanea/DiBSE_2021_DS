package mci.rest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/node-info")
public class NodeResource {

    static Logger log = LoggerFactory.getLogger(NodeResource.class);

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChordNode> getNodes() {
        log.info("getNodes() called");
        return VisualizeChordNetwork.nodeList;
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String getHello() {
        log.info("Hello called");
        return "Hello!";
    }
}
