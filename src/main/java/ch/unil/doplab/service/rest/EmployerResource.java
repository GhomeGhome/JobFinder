package ch.unil.doplab.service.rest;

import ch.unil.doplab.Employer;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.*;

@Path("/employers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployerResource {

    @Inject
    private ApplicationState state;

    // ============================================
    // GET /employers
    // ============================================
    @GET
    public Collection<Employer> all() {
        return state.getAllEmployers().values();
    }

    // ============================================
    // GET /employers/{id}
    // ============================================
    @GET
    @Path("/{id}")
    public Employer get(@PathParam("id") UUID id) {
        Employer e = state.getEmployer(id);
        if (e == null) throw new NotFoundException("Employer not found");
        return e;
    }

    // ============================================
    // POST /employers
    // ============================================
    @POST
    public Response add(Employer e, @Context UriInfo uri) {
        Employer created = state.addEmployer(e);
        URI location = uri.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();
        return Response.created(location).entity(created).build();
    }

    // ============================================
    // PUT /employers/{id}
    // ============================================
    @PUT
    @Path("/{id}")
    public Employer update(@PathParam("id") UUID id, Employer updated) {
        Employer existing = state.getEmployer(id);
        if (existing == null) throw new NotFoundException("Employer not found");

        updated.setId(id);
        state.getAllEmployers().put(id, updated);
        return updated;
    }

    // ============================================
    // DELETE /employers/{id}
    // ============================================
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        Employer removed = state.getAllEmployers().remove(id);
        if (removed == null) throw new NotFoundException("Employer not found");
        return Response.noContent().build();
    }
}
