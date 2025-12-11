package ch.unil.doplab.ui;

import ch.unil.doplab.Application;
import ch.unil.doplab.Applicant;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.Employer;
import ch.unil.doplab.client.JobFinderClient; // NEW IMPORT
// import ch.unil.doplab.service.domain.ApplicationState; // REMOVE

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

    public UUID getFilterJobOfferId() {
        return filterJobOfferId;
    }

    public void setFilterJobOfferId(UUID filterJobOfferId) {
        this.filterJobOfferId = filterJobOfferId;
    }

    public String openApplicationsForJob(UUID offerId) {
        this.filterJobOfferId = offerId;
        return "employerApplications";
    }

    public List<Application> getAllApplications() {
        // return appState.getAllApplications().values().stream()
        //         .collect(Collectors.toList());

        return client.getAllApplications(); // NEW
    }

    /**
     * Applications for job offers owned by the logged-in employer.
     */
    public List<Application> getApplicationsForLoggedEmployer() {
        if (!loginBean.isEmployer()) {
            return Collections.emptyList();
        }

        Employer current = loginBean.getLoggedEmployer();
        if (current == null || current.getId() == null) {
            return Collections.emptyList();
        }

        UUID employerId = current.getId();

        // NEW STRATEGY: Get all applications from API, then filter in memory.
        // Ideally, we would have a backend endpoint for this, but this works for now.
        List<Application> allApps = client.getAllApplications();

        return allApps.stream()
                .filter(app -> {
                    // JobOffer offer = appState.getOffer(app.getJobOfferId()); // OLD
                    JobOffer offer = client.getJobOffer(app.getJobOfferId()); // NEW

                    if (offer == null) return false;

                    // only offers of this employer
                    if (!employerId.equals(offer.getEmployerId())) {
                        return false;
                    }

                    // if a specific job is selected, filter further
                    if (filterJobOfferId != null && !filterJobOfferId.equals(offer.getId())) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    // --- Helper Methods for the UI ---

    public String getJobTitle(UUID offerId) {
        if (offerId == null) return "Unknown";
        // JobOffer offer = appState.getOffer(offerId); // OLD
        JobOffer offer = client.getJobOffer(offerId); // NEW
        return (offer != null) ? offer.getTitle() : "Offer Removed";
    }

    public String getApplicantName(UUID applicantId) {
        if (applicantId == null) return "Unknown";
        // Applicant applicant = appState.getApplicant(applicantId); // OLD
        Applicant applicant = client.getApplicant(applicantId); // NEW

        return (applicant != null)
                ? applicant.getFirstName() + " " + applicant.getLastName()
                : "User Removed";
    }

    public boolean isFilteredByJob() {
        return filterJobOfferId != null;
    }

    public String clearJobFilter() {
        this.filterJobOfferId = null;
        return null;
    }
}