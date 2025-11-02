package ch.unil.doplab.service.rest;

import ch.unil.doplab.Applicant;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Path("/applicants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicantResource {

    @Inject
    private ApplicationState state;

    // ----- Simple inline DTOs to keep Phase-1 light -----

    public static class CreateReq {
        public String username, password, firstName, lastName, email, contactInfo;
        public String descriptionInfo; // optional
        public String cvInfo;          // optional
    }

    public static class UpdateReq {
        public String firstName, lastName, email, contactInfo;
        public String descriptionInfo; // optional
        public String cvInfo;          // optional
    }

    // ---------------- CRUD ----------------

    @GET
    public Collection<Applicant> list() {
        return state.getAllApplicants().values();
    }

    @GET
    @Path("/{id}")
    public Applicant get(@PathParam("id") UUID id) {
        var a = state.getApplicant(id);
        if (a == null) throw new NotFoundException();
        return a;
    }

    @POST
    public Response create(CreateReq dto, @Context UriInfo uri) {
        if (dto == null || anyBlank(dto.username, dto.password, dto.firstName, dto.lastName, dto.email, dto.contactInfo)) {
            throw new BadRequestException("Missing required fields.");
        }
        var a = new Applicant(dto.username, dto.password, dto.firstName, dto.lastName, dto.email, dto.contactInfo, dto.descriptionInfo);
        if (notBlank(dto.cvInfo)) a.setCvInfo(dto.cvInfo);
        state.addApplicant(a);

        URI location = uri.getAbsolutePathBuilder().path(a.getId().toString()).build();
        return Response.created(location).entity(a).build(); // 201 + Location
    }

    @PUT
    @Path("/{id}")
    public Applicant update(@PathParam("id") UUID id, UpdateReq dto) {
        var existing = state.getApplicant(id);
        if (existing == null) throw new NotFoundException();
        if (dto == null) throw new BadRequestException("Body required.");

        if (notBlank(dto.firstName))    existing.setFirstName(dto.firstName);
        if (notBlank(dto.lastName))     existing.setLastName(dto.lastName);
        if (notBlank(dto.email))        existing.setEmail(dto.email);
        if (notBlank(dto.contactInfo))  existing.setContactInfo(dto.contactInfo);

        // optional: clear when blank
        existing.setDescriptionInfo(blankToNull(dto.descriptionInfo));
        existing.setCvInfo(blankToNull(dto.cvInfo));

        state.setApplicant(id, existing);
        return existing;
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        boolean removed = state.removeApplicant(id);
        if (!removed) throw new NotFoundException();
        return Response.noContent().build(); // 204
    }

    // ---------------- helpers ----------------
    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private static boolean anyBlank(String... ss) { return Arrays.stream(ss).anyMatch(s -> s == null || s.isBlank()); }
    private static String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
}
