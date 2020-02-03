package ch.so.agi.grundstuecksinformation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/ping")
public class MainController {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "grundstuecksinformation-avdpool";
    }
}