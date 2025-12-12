package ch.unil.doplab.service.rest;

import ch.unil.doplab.Application;
import ch.unil.doplab.ApplicationStatus;
import ch.unil.doplab.Applicant;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.service.domain.ApplicationState;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.*;

@Path("/applications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicationResource {

    @Inject
    private ApplicationState state;

    // ======================================================
    // GET /applications
    // ======================================================
    @GET
    public List<Application> all() {
        return new ArrayList<>(state.getAllApplications().values());
    }

    // ======================================================
    // GET /applications/{id}
    // ======================================================
    @GET
    @Path("/{id}")
    public Application get(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);
        Application a = state.getApplication(id);
        if (a == null) throw new NotFoundException("Application not found");
        return a;
    }

    // ======================================================
    // GET /applications/by-offer/{jobOfferId}
    // ======================================================
    @GET
    @Path("/by-offer/{jobOfferId}")
    public List<Application> byOffer(@PathParam("jobOfferId") String jobOfferIdStr) {
        UUID jobOfferId = UUID.fromString(jobOfferIdStr);

        return state.getAllApplications()
                .values()
                .stream()
                .filter(a -> jobOfferId.equals(a.getJobOfferId()))
                .toList();
    }

    // ======================================================
    // GET /applications/by-applicant/{applicantId}
    // ======================================================
    @GET
    @Path("/by-applicant/{applicantId}")
    public List<Application> byApplicant(@PathParam("applicantId") String applicantIdStr) {
        UUID applicantId = UUID.fromString(applicantIdStr);

        return state.getAllApplications()
                .values()
                .stream()
                .filter(a -> applicantId.equals(a.getApplicantId()))
                .toList();
    }

    // ======================================================
    // POST /applications
    // ======================================================
    @POST
    public Response add(Application a, @Context UriInfo uri) {

        // Optional but nice: validate that JobOffer & Applicant exist
//        UUID jobOfferId = a.getJobOfferId();
//        UUID applicantId = a.getApplicantId();
//
//        JobOffer offer = state.getOffer(jobOfferId);
//        if (offer == null) {
//            throw new BadRequestException("Invalid jobOfferId: " + jobOfferId);
//        }
//
//        Applicant ap = state.getApplicant(applicantId);
//        if (ap == null) {
//            throw new BadRequestException("Invalid applicantId: " + applicantId);
//        }

        Application created = state.addApplication(a);

        URI location = uri.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();

        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}/match-score")
    @Consumes(MediaType.APPLICATION_JSON)
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

        Applicant applicant = state.getApplicant(applicantId);
        if (applicant == null) throw new NotFoundException("Applicant not found");

        // all applications of this applicant
        List<Application> apps = state.getAllApplications().values().stream()
                .filter(a -> applicantId.equals(a.getApplicantId()))
                .toList();

        int updated = 0;

        for (Application app : apps) {
            JobOffer offer = state.getOffer(app.getJobOfferId());
            if (offer == null) continue;

            double score = computeMatchScore(applicant, offer);
            state.updateApplicationMatchScore(app.getId(), score);
            updated++;
        }

        return Response.ok(Map.of("updated", updated)).build();
    }

    /** Copy the same tokenize/compute you already wrote */
    private static Set<String> tokenize(String text) {
        if (text == null) return java.util.Collections.emptySet();
        String[] raw = text.toLowerCase().split("[^a-z0-9+]+");
        Set<String> tokens = new java.util.HashSet<>();
        for (String t : raw) {
            t = t.trim();
            if (t.length() >= 2) tokens.add(t);
        }
        return tokens;
    }

    private double computeMatchScore(Applicant applicant, JobOffer offer) {
        String skillsStr = applicant.getSkillsAsString();
        Set<String> skillTokens = tokenize(skillsStr);
        if (skillTokens.isEmpty()) return 0.0;

        StringBuilder jobText = new StringBuilder();
        if (offer.getTitle() != null) jobText.append(offer.getTitle()).append(" ");
        if (offer.getDescription() != null) jobText.append(offer.getDescription());

        Set<String> jobTokens = tokenize(jobText.toString());
        if (jobTokens.isEmpty()) return 0.0;

        int matches = 0;
        for (String s : skillTokens) {
            if (jobTokens.contains(s)) matches++;
        }

        double raw = (matches * 100.0) / skillTokens.size();
        return Math.round(raw * 10.0) / 10.0;
    }


    // ======================================================
    // PUT /applications/{id}
    // ======================================================
    @PUT
    @Path("/{id}")
    public Application update(@PathParam("id") String idStr, Application updated) {
        UUID id = UUID.fromString(idStr);

        Application existing = state.getApplication(id);
        if (existing == null)
            throw new NotFoundException("Application not found");

        updated.setId(id);

        // If you added a setApplication(...) in ApplicationState, use that.
        // Otherwise, update the map directly:
        state.getAllApplications().put(id, updated);

        return updated;
    }

    // ======================================================
    // DELETE /applications/{id}
    // ======================================================
    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);

        boolean ok = state.removeApplication(id);
        if (!ok) throw new NotFoundException("Application not found");

        return Response.noContent().build();
    }

    // ======================================================
    // POST /applications/{id}/status/{status}
    // ======================================================
    @POST
    @Path("/{id}/status/{status}")
    public Application updateStatus(@PathParam("id") String idStr,
                                    @PathParam("status") String statusRaw) {

        UUID id = UUID.fromString(idStr);

        ApplicationStatus status;
        try {
            // must match enum names: Submitted, In_review, Rejected, Accepted, Withdrawn
            status = ApplicationStatus.valueOf(statusRaw);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid ApplicationStatus: " + statusRaw);
        }

        Application updated = state.updateApplicationStatus(id, status);
        if (updated == null) {
            throw new NotFoundException("Application not found");
        }
        return updated;
    }
}
