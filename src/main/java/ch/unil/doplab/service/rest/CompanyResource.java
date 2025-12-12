package ch.unil.doplab.service.rest;

import ch.unil.doplab.Company;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    public List<Company> all() {
        return new ArrayList<>(state.getAllCompanies().values());
    }

    // ======================================================
    // GET /companies/{id}
    // ======================================================
    @GET
    @Path("/{id}")
    public Company get(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);
        Company c = state.getCompany(id);
        if (c == null) throw new NotFoundException("Company not found");
        return c;
    }

    // ======================================================
    // GET /companies/by-employer/{employerId}
    // ======================================================
    @GET
    @Path("/by-employer/{employerId}")
    public List<Company> byEmployer(@PathParam("employerId") String employerIdStr) {
        UUID employerId = UUID.fromString(employerIdStr);

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
        // Optional: validate that ownerEmployerId exists inside ApplicationState
        // (or do it inside state.addCompany)

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
    public Company update(@PathParam("id") String idStr, Company updated) {
        UUID id = UUID.fromString(idStr);

        boolean ok = state.setCompany(id, updated);  // <- needs to exist in ApplicationState
        if (!ok) throw new NotFoundException("Company not found");

        Company result = state.getCompany(id);
        if (result == null) throw new NotFoundException("Company not found after update");

        return result;
    }

    // ======================================================
    // DELETE /companies/{id}
    // ======================================================
    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);

        boolean removed = state.removeCompany(id);   // <- needs to exist in ApplicationState
        if (!removed) throw new NotFoundException("Company not found");

        return Response.noContent().build();
    }
}
