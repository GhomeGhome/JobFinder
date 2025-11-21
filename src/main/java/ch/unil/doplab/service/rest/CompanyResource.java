package ch.unil.doplab.service.rest;

import ch.unil.doplab.Company;
import ch.unil.doplab.Employer;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Path("/companies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyResource {

    @Inject
    private ApplicationState state;


    // ======================================================
    // GET /companies
    // ======================================================
    @GET
    public Collection<Company> all() {
        return state.getAllCompanies().values();
    }


    // ======================================================
    // GET /companies/{id}
    // ======================================================
    @GET
    @Path("/{id}")
    public Company get(@PathParam("id") UUID id) {
        Company c = state.getCompany(id);
        if (c == null) throw new NotFoundException("Company not found");
        return c;
    }


    // ======================================================
    // GET /companies/by-employer/{employerId}
    // ======================================================
    @GET
    @Path("/by-employer/{employerId}")
    public List<Company> byEmployer(@PathParam("employerId") UUID employerId) {
        return state.getAllCompanies()
                .values()
                .stream()
                .filter(c -> employerId.equals(c.getOwnerEmployerId()))
                .collect(Collectors.toList());
    }


    // ======================================================
    // POST /companies
    // ======================================================
    @POST
    public Response add(Company c, @Context UriInfo uri) {

        // Vérifier si employer existe
        UUID ownerId = c.getOwnerEmployerId();
        if (ownerId != null) {
            Employer owner = state.getEmployer(ownerId);
            if (owner == null)
                throw new BadRequestException("Owner employer not found: " + ownerId);
        }

        Company created = state.addCompany(c);
        URI location = uri.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();

        return Response.created(location).entity(created).build();
    }


    // ======================================================
    // PUT /companies/{id}
    // ======================================================
    @PUT
    @Path("/{id}")
    public Company update(@PathParam("id") UUID id, Company updated) {

        Company existing = state.getCompany(id);
        if (existing == null)
            throw new NotFoundException("Company not found");

        updated.setId(id);
        state.getAllCompanies().put(id, updated);

        return updated;
    }


    // ======================================================
    // DELETE /companies/{id}
    // ======================================================
    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") UUID id) {

        Company removed = state.getAllCompanies().remove(id);
        if (removed == null)
            throw new NotFoundException("Company not found");

        return Response.noContent().build();
    }
}
