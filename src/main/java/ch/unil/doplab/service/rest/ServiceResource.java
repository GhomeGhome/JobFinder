package ch.unil.doplab.service.rest;

import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;

@Path("/service")
public class ServiceResource {

    @Inject
    private ApplicationState state;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/populateDB")
    public Response populateDB() {
        state.populateDB();
        return Response.ok("JobFinder database was populated at " + LocalDateTime.now()).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/clearDB")
    public Response clearDB() {
        try {
            state.clearDB();
            return Response.ok("JobFinder database was cleared at " + LocalDateTime.now()).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/resetDB")
    public Response resetDB() {
        state.resetDB();
        return Response.ok("JobFinder database was reset at " + LocalDateTime.now()).build();
    }
}
