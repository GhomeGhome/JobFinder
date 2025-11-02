package ch.unil.doplab.service.rest;

import ch.unil.doplab.JobOffer;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Path("/job-offers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobOfferResource {

    @Inject
    private ApplicationState state;

    @GET
    public List<JobOffer> all(@QueryParam("employerId") UUID employerId) {
        var values = state.getAllOffers().values();
        if (employerId == null) return new ArrayList<>(values);
        return values.stream()
                .filter(o -> employerId.equals(o.getEmployerId()))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public JobOffer get(@PathParam("id") UUID id) {
        var o = state.getOffer(id);
        if (o == null) throw new NotFoundException();
        return o;
    }

    @POST
    public Response add(JobOffer offer, @Context UriInfo uri) {
        // ApplicationState assigns id, status/createdAt defaults, and validates employerId
        JobOffer created = state.addOffer(offer);
        URI location = uri.getAbsolutePathBuilder().path(created.getId().toString()).build();
        return Response.created(location).entity(created).build(); // 201 + Location
    }

    @PUT
    @Path("/{id}")
    public JobOffer update(@PathParam("id") UUID id, JobOffer offer) {
        boolean ok = state.setOffer(id, offer);
        if (!ok) throw new NotFoundException();
        // Return the updated entity
        var updated = state.getOffer(id);
        if (updated == null) throw new NotFoundException();
        return updated;
    }

    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") UUID id) {
        boolean removed = state.removeOffer(id);
        if (!removed) throw new NotFoundException();
        return Response.noContent().build(); // 204
    }

    // Owner-only: publish a DRAFT offer
    @POST
    @Path("/{id}/publish/{employerId}")
    public JobOffer publish(@PathParam("id") UUID offerId,
                            @PathParam("employerId") UUID employerId) {
        return state.publishOffer(offerId, employerId);
    }
}
