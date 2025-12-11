package ch.unil.doplab.client;

import ch.unil.doplab.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JobFinderClient {

    private final String BASE_URL = "http://localhost:8080/jobfinder/api";
    private final Client client;

    public JobFinderClient() {
        this.client = ClientBuilder.newClient();
    }

    // ==========================================
    // JOB OFFERS
    // ==========================================

    public List<JobOffer> getAllJobOffers() {
        return client.target(BASE_URL + "/job-offers")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<JobOffer>>() {});
    }

    public List<JobOffer> getOffersByEmployer(UUID employerId) {
        return client.target(BASE_URL + "/job-offers")
                .queryParam("employerId", employerId)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<JobOffer>>() {});
    }

    public JobOffer getJobOffer(UUID id) {
        return client.target(BASE_URL + "/job-offers/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get(JobOffer.class);
    }

    // ==========================================
    // COMPANIES
    // ==========================================

    public List<Company> getAllCompanies() {
        return client.target(BASE_URL + "/companies")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Company>>() {});
    }

    public Company getCompany(UUID id) {
        try {
            return client.target(BASE_URL + "/companies/" + id)
                    .request(MediaType.APPLICATION_JSON)
                    .get(Company.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ==========================================
    // EMPLOYERS
    // ==========================================

    public List<Employer> getAllEmployers() {
        return client.target(BASE_URL + "/employers")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Employer>>() {});
    }

    // ==========================================
    // APPLICANTS
    // ==========================================

    public List<Applicant> getAllApplicants() {
        // NOTE: Ensure ApplicantResource exists at /applicants
        return client.target(BASE_URL + "/applicants")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Applicant>>() {});
    }

    public Applicant getApplicant(UUID id) {
        return client.target(BASE_URL + "/applicants/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get(Applicant.class);
    }

    // ==========================================
    // APPLICATIONS
    // ==========================================

    public List<Application> getAllApplications() {
        return client.target(BASE_URL + "/applications")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Application>>() {});
    }

    public void updateApplicationStatus(UUID applicationId, String status) {
        // POST /applications/{id}/status/{status}
        client.target(BASE_URL + "/applications/" + applicationId + "/status/" + status)
                .request()
                .post(Entity.json("")); // Empty body as params are in URL
    }
}