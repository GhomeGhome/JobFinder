package ch.unil.doplab.service.rest;

import ch.unil.doplab.Application;
import ch.unil.doplab.ApplicationStatus;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.Applicant;
import ch.unil.doplab.service.domain.ApplicationState;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

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
    public Collection<Application> all() {
        return state.getAllApplications().values();
    }


    // ======================================================
    // GET /applications/{id}
    // ======================================================
    @GET
    @Path("/{id}")
    public Application get(@PathParam("id") UUID id) {
        Application a = state.getApplication(id);
        if (a == null) throw new NotFoundException("Application not found");
        return a;
    }


    // ======================================================
    // GET /applications/by-offer/{jobOfferId}
    // ======================================================
    @GET
    @Path("/by-offer/{jobOfferId}")
    public List<Application> byOffer(@PathParam("jobOfferId") UUID jobOfferId) {
        return state.getAllApplications()
                .values()
                .stream()
                .filter(a -> jobOfferId.equals(a.getJobOfferId()))
                .collect(Collectors.toList());
    }


    // ======================================================
    // GET /applications/by-applicant/{applicantId}
    // ======================================================
    @GET
    @Path("/by-applicant/{applicantId}")
    public List<Application> byApplicant(@PathParam("applicantId") UUID applicantId) {
        return state.getAllApplications()
                .values()
                .stream()
                .filter(a -> applicantId.equals(a.getApplicantId()))
                .collect(Collectors.toList());
    }


    // ======================================================
    // POST /applications
    // ======================================================
    @POST
    public Response add(Application a, @Context UriInfo uri) {

        // Valider JobOffer
        JobOffer o = state.getOffer(a.getJobOfferId());
        if (o == null)
            throw new BadRequestException("Invalid jobOfferId: " + a.getJobOfferId());

        // Valider Applicant
        Applicant ap = state.getApplicant(a.getApplicantId());
        if (ap == null)
            throw new BadRequestException("Invalid applicantId: " + a.getApplicantId());

        Application created = state.addApplication(a);

        URI location = uri.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();

        return Response.created(location).entity(created).build();
    }


    // ======================================================
    // PUT /applications/{id}
    // ======================================================
    @PUT
    @Path("/{id}")
    public Application update(@PathParam("id") UUID id, Application updated) {

        Application existing = state.getApplication(id);
        if (existing == null)
            throw new NotFoundException("Application not found");

        updated.setId(id);
        state.getAllApplications().put(id, updated);

        return updated;
    }


    // ======================================================
    // DELETE /applications/{id}
    // ======================================================
    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") UUID id) {

        boolean ok = state.removeApplication(id);
        if (!ok) throw new NotFoundException("Application not found");

        return Response.noContent().build();
    }


    // ======================================================
    // POST /applications/{id}/status/{newStatus}
    // ======================================================
    @POST
    @Path("/{id}/status/{status}")
    public Application updateStatus(@PathParam("id") UUID id,
                                    @PathParam("status") String statusRaw) {

        ApplicationStatus status;
        try {
            status = ApplicationStatus.valueOf(statusRaw);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid ApplicationStatus: " + statusRaw);
        }

        return state.updateApplicationStatus(id, status);
    }
}
