package ch.unil.doplab.service.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@ApplicationScoped
@Path("/ping")
public class PingResource {
    @GET public String ping() {return "ponggg";}
}
