package ch.unil.doplab.service.rest;

import ch.unil.doplab.Employer;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/employers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployerResource {

    @Inject
    private ApplicationState state;

    @GET
    public List<Employer> all() {
        return state.listEmployers();
    }

    @GET
    @Path("/{id}")
    public Employer get(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);
        Employer e = state.findEmployer(id);
        if (e == null) throw new NotFoundException("Employer not found");
        return e;
    }

    @POST
    public Response add(Employer e, @Context UriInfo uri) {
        Employer created = state.createEmployer(e);

        URI location = uri.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();

        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Employer update(@PathParam("id") String idStr, Employer updated) {
        UUID id = UUID.fromString(idStr);

        Employer result = state.updateEmployer(id, updated);
        if (result == null) throw new NotFoundException("Employer not found");

        return result;
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);

        boolean removed = state.deleteEmployer(id);
        if (!removed) throw new NotFoundException("Employer not found");

        return Response.noContent().build();
    }
}
