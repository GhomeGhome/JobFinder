package ch.unil.doplab.client;

import ch.unil.doplab.Employer;
import ch.unil.doplab.Company;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.Application;
import ch.unil.doplab.Applicant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.client.Entity;


import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class JobFinderClient {
    private WebTarget target;
    private static final String BASE_URL = "http://localhost:8080/jobfinder/api";
    private Client client;

    public JobFinderClient() {
        this.client = ClientBuilder.newClient();
        this.target = client.target(BASE_URL);
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

    public JobOffer createJobOffer(JobOffer offer) {
        Response response = null;
        try {
            response = target.path("job-offers")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(offer));

            int status = response.getStatus();
            System.out.println("POST /job-offers status = " + status);

            if (status >= 200 && status < 300) {
                return response.readEntity(JobOffer.class);
            } else {
                String body = "";
                try {
                    body = response.readEntity(String.class);
                } catch (Exception ignored) {}
                System.err.println("Error creating job offer: " + status + " body=" + body);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (response != null) response.close();
        }
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
        return target.path("companies")
                .path(id.toString())
                .request(MediaType.APPLICATION_JSON)
                .get(Company.class);
    }

    // ==========================================
    // EMPLOYERS
    // ==========================================
    public Employer getEmployer(UUID id) {
        return target.path("employers")
                .path(id.toString())
                .request(MediaType.APPLICATION_JSON)
                .get(Employer.class);
    }

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

    public Application updateApplicationMatchScore(UUID applicationId, double score) {
        return client.target(BASE_URL + "/applications/" + applicationId + "/match-score")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(Map.of("matchScore", score)), Application.class);
    }

    public void recomputeMatchScoresForApplicant(UUID applicantId) {
        client.target(BASE_URL + "/applications/recompute/by-applicant/" + applicantId)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""));
    }


    // ==========================================
    // APPLICATIONS
    // ==========================================

    public List<Application> getAllApplications() {
        return client.target(BASE_URL + "/applications")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Application>>() {});
    }

    public boolean createApplication(Application app) {
        Response response = null;
        try {
            response = target.path("applications")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(app));

            int status = response.getStatus();
            System.out.println("POST /applications status = " + status);
            String body = "";
            try {
                body = response.readEntity(String.class);
            } catch (Exception ignored) {}
            System.out.println("POST /applications body = " + body);

            // consider any 2xx as success
            return status >= 200 && status < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public void updateApplicationStatus(UUID applicationId, String status) {
        // POST /applications/{id}/status/{status}
        client.target(BASE_URL + "/applications/" + applicationId + "/status/" + status)
                .request()
                .post(Entity.json("")); // Empty body as params are in URL
    }

    public List<Application> getApplicationsByOffer(UUID offerId) {
        return client.target(BASE_URL + "/applications/by-offer/" + offerId)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Application>>() {});
    }

    public Applicant createApplicant(Applicant a) {
        Response response = null;
        try {
            response = target.path("applicants")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(a));
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                return response.readEntity(Applicant.class);
            }
            return null;
        } finally {
            if (response != null) response.close();
        }
    }

    public Employer createEmployer(Employer e) {
        Response response = null;
        try {
            response = target.path("employers")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(e));
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                return response.readEntity(Employer.class);
            }
            return null;
        } finally {
            if (response != null) response.close();
        }
    }

    public boolean updateApplicant(Applicant app) {
        try {
            Response response = target.path("applicants")
                    .path(app.getId().toString())
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.json(app));

            int status = response.getStatus();
            response.close();
            return status >= 200 && status < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEmployer(Employer emp) {
        try {
            Response response = target.path("employers")
                    .path(emp.getId().toString())
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.json(emp));

            int status = response.getStatus();
            response.close();
            return status >= 200 && status < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCompany(Company company) {
        try {
            Response response = target.path("companies")
                    .path(company.getId().toString())
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.json(company));

            int status = response.getStatus();
            response.close();
            return status >= 200 && status < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== EXTERNAL APIS =====

    public List<Map<String, Object>> searchRemoteOk(String keyword, int limit) {
        return client.target(BASE_URL + "/external/remoteok")
                .queryParam("keyword", keyword)
                .queryParam("limit", limit)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Map<String, Object>>>() {});
    }

    public List<Map<String, Object>> suggestSkills(String q,
                                                   String type,
                                                   int limit,
                                                   String lang) {
        return client.target(BASE_URL + "/skills/suggest")
                .queryParam("q", q)
                .queryParam("type", type)
                .queryParam("limit", limit)
                .queryParam("lang", lang)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Map<String, Object>>>() {});
    }

    // Add this to JobFinderClient.java
    public boolean updateApplication(ch.unil.doplab.Application app) {
        try {
            // Debug print to confirm it is being called
            System.out.println(">>> CLIENT: Sending PUT request for Application ID: " + app.getId() + " with Status: " + app.getStatus());

            target.path("applications")
                    .path(app.getId().toString())
                    .request(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                    .put(jakarta.ws.rs.client.Entity.json(app));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}