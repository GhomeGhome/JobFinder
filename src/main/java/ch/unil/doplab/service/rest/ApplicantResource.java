package ch.unil.doplab.service.rest;

import ch.unil.doplab.Applicant;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.*;

@Path("/applicants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicantResource {

    @Inject
    private ApplicationState state;

    // ============================================
    // GET /applicants
    // ============================================
    @GET
    public Collection<Applicant> all() {
        return state.getAllApplicants().values();
    }

    // ============================================
    // GET /applicants/{id}
    // ============================================
    @GET
    @Path("/{id}")
    public Applicant get(@PathParam("id") UUID id) {
        Applicant a = state.getApplicant(id);
        if (a == null) throw new NotFoundException("Applicant not found");
        return a;
    }

    // ============================================
    // POST /applicants
    // ============================================
    @POST
    public Response add(Applicant a, @Context UriInfo uri) {
        Applicant created = state.addApplicant(a);
        URI location = uri.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();
        return Response.created(location).entity(created).build();
    }

    // ============================================
    // PUT /applicants/{id}
    // ============================================
    @PUT
    @Path("/{id}")
    public Applicant update(@PathParam("id") UUID id, Applicant updated) {
        Applicant existing = state.getApplicant(id);
        if (existing == null) throw new NotFoundException("Applicant not found");

        updated.setId(id);
        state.getAllApplicants().put(id, updated);
        return updated;
    }

    // ============================================
    // DELETE /applicants/{id}
    // ============================================
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        Applicant removed = state.getAllApplicants().remove(id);
        if (removed == null) throw new NotFoundException("Applicant not found");
        return Response.noContent().build();
    }
}