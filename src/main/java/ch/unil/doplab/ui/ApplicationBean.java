package ch.unil.doplab.ui;

import ch.unil.doplab.Application;
import ch.unil.doplab.Applicant;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.Employer;
import ch.unil.doplab.client.JobFinderClient;
// import ch.unil.doplab.service.domain.ApplicationState; // REMOVED

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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

        // 2. Filter in memory
        return allApps.stream()
                .filter(app -> {
                    // Fetch the job for this application to check ownership
                    JobOffer offer = client.getJobOffer(app.getJobOfferId());

                    if (offer == null) return false; // Orphaned application

                    // CHECK A: Is this job owned by the logged-in employer?
                    if (!employerId.equals(offer.getEmployerId())) {
                        return false;
                    }

                    // CHECK B: Are we filtering by a specific job ID?
                    if (filterJobOfferId != null && !filterJobOfferId.equals(offer.getId())) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
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