package ch.unil.doplab.service.rest;

import ch.unil.doplab.JobOffer;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

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

    @GET
    public List<JobOffer> all(@QueryParam("employerId") UUID employerId) {
        var values = state.getAllOffers().values();

        if (employerId == null) {
            return new ArrayList<>(values);
        }

        return values.stream()
                .filter(o -> employerId.equals(o.getEmployerId()))
                .collect(Collectors.toList());
    }


    // ======================================================
    // GET /job-offers/{id}
    // ======================================================

    @GET
    @Path("/{id}")
    public JobOffer get(@PathParam("id") UUID id) {
        var o = state.getOffer(id);
        if (o == null) throw new NotFoundException("JobOffer not found");
        return o;
    }


    // ======================================================
    // POST /job-offers
    // ======================================================

    @POST
    public Response add(JobOffer offer, @Context UriInfo uriInfo) {
        JobOffer created = state.addOffer(offer);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();

        return Response.created(location)
                .entity(created)
                .build();   // 201 Created + Location
    }


    // ======================================================
    // PUT /job-offers/{id}
    // Remplace intégralement l'offre (sauf ID)
    // ======================================================

    @PUT
    @Path("/{id}")
    public JobOffer update(@PathParam("id") UUID id, JobOffer offer) {
        boolean ok = state.setOffer(id, offer);
        if (!ok) throw new NotFoundException("JobOffer not found");

        JobOffer updated = state.getOffer(id);
        if (updated == null) throw new NotFoundException("JobOffer not found after update");
        return updated;
    }


    // ======================================================
    // DELETE /job-offers/{id}
    // ======================================================

    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") UUID id) {
        boolean removed = state.removeOffer(id);
        if (!removed) throw new NotFoundException("JobOffer not found");

        return Response.noContent().build(); // 204 No Content
    }


    // ======================================================
    // POST /job-offers/{id}/publish/{employerId}
    // ======================================================

    @POST
    @Path("/{id}/publish/{employerId}")
    public JobOffer publish(@PathParam("id") UUID offerId,
                            @PathParam("employerId") UUID employerId) {
        try {
            return state.publishOffer(offerId, employerId);
        } catch (NoSuchElementException ex) {
            throw new NotFoundException("JobOffer not found");
        } catch (SecurityException ex) {
            throw new ForbiddenException(ex.getMessage());
        }
    }


    // ======================================================
    // POST /job-offers/{id}/close/{employerId}
    // ======================================================

    @POST
    @Path("/{id}/close/{employerId}")
    public JobOffer close(@PathParam("id") UUID offerId,
                          @PathParam("employerId") UUID employerId) {
        try {
            return state.closeOffer(offerId, employerId);
        } catch (NoSuchElementException ex) {
            throw new NotFoundException("JobOffer not found");
        } catch (SecurityException ex) {
            throw new ForbiddenException(ex.getMessage());
        }
    }


    // ======================================================
    // POST /job-offers/{id}/reopen/{employerId}
    // ======================================================

    @POST
    @Path("/{id}/reopen/{employerId}")
    public JobOffer reopen(@PathParam("id") UUID offerId,
                           @PathParam("employerId") UUID employerId) {
        try {
            return state.reopenOffer(offerId, employerId);
        } catch (NoSuchElementException ex) {
            throw new NotFoundException("JobOffer not found");
        } catch (SecurityException ex) {
            throw new ForbiddenException(ex.getMessage());
        }
    }
}
