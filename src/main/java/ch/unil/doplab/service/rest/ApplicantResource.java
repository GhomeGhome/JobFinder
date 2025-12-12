package ch.unil.doplab.service.rest;

import ch.unil.doplab.Applicant;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/applicants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicantResource {

    @Inject
    private ApplicationState state;

    @GET
    public List<Applicant> getAll() {
        return new ArrayList<>(state.getAllApplicants().values());
    }

    @GET
    @Path("/{id}")
    public Applicant getById(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);
        Applicant a = state.getApplicant(id);
        if (a == null) throw new NotFoundException("Applicant not found");
        return a;
    }

    @POST
    public Response create(Applicant applicant, @Context UriInfo uriInfo) {
        Applicant created = state.addApplicant(applicant);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();

        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Applicant update(@PathParam("id") String idStr, Applicant updated) {
        UUID id = UUID.fromString(idStr);

        boolean ok = state.setApplicant(id, updated);   // needs setApplicant in ApplicationState
        if (!ok) {
            throw new NotFoundException("Applicant not found");
        }

        return state.getApplicant(id);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);

        boolean ok = state.removeApplicant(id);        // needs removeApplicant in ApplicationState
        if (!ok) throw new NotFoundException("Applicant not found");

        return Response.noContent().build();
    }
}
