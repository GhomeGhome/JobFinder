package ch.unil.doplab.service.rest;

import ch.unil.doplab.JobOffer;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * REST resource pour la gestion des JobOffers.
 */
@Path("/job-offers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobOfferResource {

    @Inject
    private ApplicationState state;

    // ======================================================
    // GET /job-offers?employerId=...
    // ======================================================

    // GET /job-offers?employerId=...
    @GET
    public List<JobOffer> all(@QueryParam("employerId") String employerIdStr) {
        if (employerIdStr == null || employerIdStr.isBlank()) {
            return state.listJobOffers(null);
        }
        UUID employerId = UUID.fromString(employerIdStr);
        return state.listJobOffers(employerId);
    }

    // GET /job-offers/{id}
    @GET
    @Path("/{id}")
    public JobOffer get(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);
        JobOffer o = state.findJobOffer(id);
        if (o == null) throw new NotFoundException("JobOffer not found");
        return o;
    }

    // POST /job-offers
    @POST
    public Response add(JobOffer offer, @Context UriInfo uriInfo) {
        JobOffer created = state.createJobOffer(offer);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();

        return Response.created(location)
                .entity(created)
                .build();
    }

    // PUT /job-offers/{id}
    @PUT
    @Path("/{id}")
    public JobOffer update(@PathParam("id") String idStr, JobOffer offer) {
        UUID id = UUID.fromString(idStr);
        JobOffer updated = state.updateJobOffer(id, offer);
        if (updated == null) throw new NotFoundException("JobOffer not found");
        return updated;
    }

    // DELETE /job-offers/{id}
    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);
        boolean removed = state.deleteJobOffer(id);
        if (!removed) throw new NotFoundException("JobOffer not found");

        return Response.noContent().build();
    }

    // POST /job-offers/{id}/publish/{employerId}
    @POST
    @Path("/{id}/publish/{employerId}")
    public JobOffer publish(@PathParam("id") String offerIdStr,
                            @PathParam("employerId") String employerIdStr) {

        UUID offerId = UUID.fromString(offerIdStr);
        UUID employerId = UUID.fromString(employerIdStr);

        try {
            return state.publishJobOffer(offerId, employerId);
        } catch (NoSuchElementException ex) {
            throw new NotFoundException("JobOffer not found");
        } catch (SecurityException ex) {
            throw new ForbiddenException(ex.getMessage());
        }
    }
}
