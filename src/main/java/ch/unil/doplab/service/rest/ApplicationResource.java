package ch.unil.doplab.service.rest;

import ch.unil.doplab.Application;
import ch.unil.doplab.ApplicationStatus;
import ch.unil.doplab.service.domain.ApplicationState;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Path("/applications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicationResource {

    @Inject
    private ApplicationState state;

    @GET
    public List<Application> all() {
        return state.listApplications();
    }

    @GET
    @Path("/{id}")
    public Application get(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);
        Application a = state.findApplication(id);
        if (a == null) throw new NotFoundException("Application not found");
        return a;
    }

    @GET
    @Path("/by-offer/{jobOfferId}")
    public List<Application> byOffer(@PathParam("jobOfferId") String jobOfferIdStr) {
        UUID jobOfferId = UUID.fromString(jobOfferIdStr);
        return state.listApplicationsByOfferId(jobOfferId);
    }

    @GET
    @Path("/by-applicant/{applicantId}")
    public List<Application> byApplicant(@PathParam("applicantId") String applicantIdStr) {
        UUID applicantId = UUID.fromString(applicantIdStr);
        return state.listApplicationsByApplicantId(applicantId);
    }

    @POST
    public Response add(Application a, @Context UriInfo uri) {
        Application created = state.createApplication(a);

        URI location = uri.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();

        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}/match-score")
    public Application updateMatchScore(@PathParam("id") UUID id, Map<String, Object> body) {
        Object raw = body.get("matchScore");
        if (raw == null) throw new BadRequestException("matchScore is required");

        double score = ((Number) raw).doubleValue();
        return state.updateApplicationMatchScore(id, score);
    }

    @POST
    @Path("/recompute/by-applicant/{applicantId}")
    public Response recomputeForApplicant(@PathParam("applicantId") String applicantIdStr) {
        UUID applicantId = UUID.fromString(applicantIdStr);
        int updated = state.recomputeMatchScoresForApplicant(applicantId);
        return Response.ok(Map.of("updated", updated)).build();
    }

    @PUT
    @Path("/{id}")
    public Application update(@PathParam("id") String idStr, Application updated) {
        UUID id = UUID.fromString(idStr);
        Application result = state.updateApplication(id, updated);
        if (result == null) throw new NotFoundException("Application not found");
        return result;
    }

    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);

        boolean ok = state.deleteApplication(id);
        if (!ok) throw new NotFoundException("Application not found");

        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/status/{status}")
    public Application updateStatus(@PathParam("id") String idStr,
                                    @PathParam("status") String statusRaw) {

        UUID id = UUID.fromString(idStr);

        ApplicationStatus status;
        try {
            status = ApplicationStatus.valueOf(statusRaw);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid ApplicationStatus: " + statusRaw);
        }

        try {
            return state.updateApplicationStatus(id, status);
        } catch (NoSuchElementException e) {
            throw new NotFoundException("Application not found");
        }
    }
}
