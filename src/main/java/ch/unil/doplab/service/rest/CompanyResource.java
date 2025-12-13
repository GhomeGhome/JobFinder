package ch.unil.doplab.service.rest;

import ch.unil.doplab.Company;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/companies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyResource {

    @Inject
    private ApplicationState state;

    @GET
    public List<Company> all() {
        return state.listCompanies();
    }

    @GET
    @Path("/{id}")
    public Company get(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);
        Company c = state.findCompany(id);
        if (c == null) throw new NotFoundException("Company not found");
        return c;
    }

    @GET
    @Path("/by-employer/{employerId}")
    public List<Company> byEmployer(@PathParam("employerId") String employerIdStr) {
        UUID employerId = UUID.fromString(employerIdStr);
        return state.listCompaniesByOwnerEmployerId(employerId);
    }

    @POST
    public Response add(Company c, @Context UriInfo uri) {
        Company created = state.createCompany(c);

        URI location = uri.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();

        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Company update(@PathParam("id") String idStr, Company updated) {
        UUID id = UUID.fromString(idStr);

        Company result = state.updateCompany(id, updated);
        if (result == null) throw new NotFoundException("Company not found");

        return result;
    }

    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);

        boolean removed = state.deleteCompany(id);
        if (!removed) throw new NotFoundException("Company not found");

        return Response.noContent().build();
    }
}
