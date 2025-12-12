package ch.unil.doplab.service.rest;

import ch.unil.doplab.Employer;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public List<Employer> all() {
        return new ArrayList<>(state.getAllEmployers().values());
    }

    // ============================================
    // GET /employers/{id}
    // ============================================
    @GET
    @Path("/{id}")
    public Employer get(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);
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
    public Employer update(@PathParam("id") String idStr, Employer updated) {
        UUID id = UUID.fromString(idStr);

        boolean ok = state.setEmployer(id, updated);   // needs setEmployer in ApplicationState
        if (!ok) {
            throw new NotFoundException("Employer not found");
        }

        return state.getEmployer(id);
    }

    // ============================================
    // DELETE /employers/{id}
    // ============================================
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);

        boolean removed = state.removeEmployer(id);    // needs removeEmployer in ApplicationState
        if (!removed) throw new NotFoundException("Employer not found");

        return Response.noContent().build();
    }
}
