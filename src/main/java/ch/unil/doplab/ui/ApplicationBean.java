package ch.unil.doplab.ui;

import ch.unil.doplab.Application;
import ch.unil.doplab.Applicant;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.Employer;
import ch.unil.doplab.client.JobFinderClient;

import java.util.*;
import java.util.ArrayList;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.stream.Collectors;

@Named("applicationBean")
@SessionScoped
public class ApplicationBean implements Serializable {

    // @Inject
    // private ApplicationState appState; // OLD

    @Inject
    private JobFinderClient client; // NEW

    @Inject
    private LoginBean loginBean;

    private UUID filterJobOfferId;

    // --- Actions ---

    public String updateStatus(ch.unil.doplab.Application app) {
        if (app != null) {
            // 1. Send the update to the server
            boolean success = client.updateApplication(app);

            if (success) {
                // 2. CRITICAL STEP: Refresh the local session data
                // We ask the server for the fresh Employer object (which includes the updated application list)
                ch.unil.doplab.Employer freshEmployer = client.getEmployer(loginBean.getLoggedEmployer().getId());

                // 3. Update the LoginBean so the page shows the new data
                if (freshEmployer != null) {
                    loginBean.setLoggedEmployer(freshEmployer);
                }

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Status updated to " + app.getStatus()));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not update status."));
            }
        }
        return null; // Reloads the page with the fresh data
    }

    /**
     * Called when clicking "View applicants" on the dashboard.
     */
    public String openApplicationsForJob(UUID offerId) {
        this.filterJobOfferId = offerId;
        // Navigate to the applications page
        return "employerApplications?faces-redirect=true&includeViewParams=true";
    }

    public String clearJobFilter() {
        this.filterJobOfferId = null;
        // Reload page without filter
        return "employerApplications?faces-redirect=true";
    }

    // --- Data Access ---

    public List<Application> getAllApplications() {
        return client.getAllApplications();
    }

    /**
     * Main list for 'employerApplications.xhtml'.
     * Filters: 1. By Employer Owner, 2. By Specific Job (if selected).
     */
    public List<Application> getApplicationsForLoggedEmployer() {
        // Security check
        if (!loginBean.isEmployer() || loginBean.getLoggedEmployer() == null) {
            return Collections.emptyList();
        }

        UUID employerId = loginBean.getLoggedEmployer().getId();

        // 1. Fetch EVERYTHING from server
        List<Application> allApps = client.getAllApplications();

        List<Application> result = new ArrayList<>();

        for (Application app : allApps) {
            // Fetch the job for this application to check ownership
            JobOffer offer = client.getJobOffer(app.getJobOfferId());
            if (offer == null) continue; // Orphaned application

            // CHECK A: Is this job owned by the logged-in employer?
            if (!employerId.equals(offer.getEmployerId())) {
                continue;
            }

            // CHECK B: Are we filtering by a specific job ID?
            if (filterJobOfferId != null && !filterJobOfferId.equals(offer.getId())) {
                continue;
            }

            // --- compute + persist match score if missing ---
            if (app.getMatchScore() == null) {
                Applicant applicant = client.getApplicant(app.getApplicantId());
                if (applicant != null) {
                    double score = computeMatchScore(applicant, offer);

                    app.setMatchScore(score); // UI immediate
//                    client.updateApplicationMatchScore(app.getId(), score); // save to DB/API
                }
            }

            result.add(app);
        }

        return result;
    }

    // --- Helpers for UI ---

    public String getJobTitle(UUID offerId) {
        if (offerId == null) return "Unknown";
        JobOffer offer = client.getJobOffer(offerId);
        return (offer != null) ? offer.getTitle() : "Offer Removed";
    }

    public String getApplicantName(UUID applicantId) {
        if (applicantId == null) return "Unknown";
        Applicant applicant = client.getApplicant(applicantId);
        return (applicant != null)
                ? applicant.getFirstName() + " " + applicant.getLastName()
                : "User Removed";
    }

    // -----------------------------
// Matching helpers (UI fallback)
// -----------------------------
    private static Set<String> tokenize(String text) {
        if (text == null) return Collections.emptySet();

        String[] raw = text
                .toLowerCase()
                .split("[^a-z0-9+]+");

        Set<String> tokens = new HashSet<>();
        for (String t : raw) {
            t = t.trim();
            if (t.length() >= 2) {
                tokens.add(t);
            }
        }
        return tokens;
    }

    private double computeMatchScore(Applicant applicant, JobOffer offer) {
        if (applicant == null || offer == null) return 0.0;

        String skillsStr = applicant.getSkillsAsString();
        Set<String> skillTokens = tokenize(skillsStr);

        if (skillTokens.isEmpty()) {
            return 0.0;
        }

        StringBuilder jobText = new StringBuilder();
        if (offer.getTitle() != null) jobText.append(offer.getTitle()).append(" ");
        if (offer.getDescription() != null) jobText.append(offer.getDescription());

        Set<String> jobTokens = tokenize(jobText.toString());
        if (jobTokens.isEmpty()) {
            return 0.0;
        }

        int matches = 0;
        for (String s : skillTokens) {
            if (jobTokens.contains(s)) {
                matches++;
            }
        }

        double raw = (matches * 100.0) / skillTokens.size();
        return Math.round(raw * 10.0) / 10.0;   // e.g. 83.3
    }


    // --- Getters/Setters ---

    public UUID getFilterJobOfferId() {
        return filterJobOfferId;
    }

    public void setFilterJobOfferId(UUID filterJobOfferId) {
        this.filterJobOfferId = filterJobOfferId;
    }

    public boolean isFilteredByJob() {
        return filterJobOfferId != null;
    }
}