package ch.unil.doplab.service.rest;

import ch.unil.doplab.JobOffer;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.*;
import java.util.stream.Collectors;

@Path("/job-offers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobOfferResource {

    @Inject
    private ApplicationState state;

    @GET
    public List<JobOffer> all() {
        return new ArrayList<>(state.getAllOffers().values());
    }

    @GET
    @Path("/{id}")
    public JobOffer get(@PathParam("id") UUID id) {
        var o = state.getOffer(id);
        if (o == null) throw new NotFoundException();
        return o;
    }

    @GET
    @Path("/by-employer/{employerId}")
    public List<JobOffer> byEmployer(@PathParam("employerId") UUID employerId) {
        return state.getAllOffers().values().stream()
                .filter(o -> employerId.equals(o.getEmployerId()))
                .collect(Collectors.toList());
    }

    @POST
    public JobOffer add(JobOffer offer) {
        // id is assigned by ApplicationState when missing
        return state.addOffer(offer);
    }

    @PUT
    @Path("/{id}")
    public boolean update(@PathParam("id") UUID id, JobOffer offer) {
        return state.setOffer(id, offer);
    }

    @DELETE
    @Path("/{id}")
    public boolean remove(@PathParam("id") UUID id) {
        return state.removeOffer(id);
    }

    // this is owner only so he can publish a draft
    @POST
    @Path("/{id}/publish/{employerId}")
    public JobOffer publish(@PathParam("id") UUID offerId,
                            @PathParam("employerId") UUID employerId) {
        return state.publishOffer(offerId, employerId);
    }
}
