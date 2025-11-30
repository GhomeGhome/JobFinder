package ch.unil.doplab.ui;

import ch.unil.doplab.Application;
import ch.unil.doplab.Applicant;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.Employer;
import ch.unil.doplab.service.domain.ApplicationState;

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

    @Inject
    private ApplicationState appState;

    @Inject
    private LoginBean loginBean;   // NEW

    // ---- NEW: optional filter by job for the employer view ----
    private UUID filterJobOfferId;

    public UUID getFilterJobOfferId() {
        return filterJobOfferId;
    }

    public void setFilterJobOfferId(UUID filterJobOfferId) {
        this.filterJobOfferId = filterJobOfferId;
    }

    // Called from "View applicants" link in employerDashboard.xhtml
    public String openApplicationsForJob(UUID offerId) {
        this.filterJobOfferId = offerId;
        // go to employerApplications.xhtml (same outcome name as your page)
        return "employerApplications";
    }

    // ----------------------------------------------------------------

    public List<Application> getAllApplications() {
        return appState.getAllApplications().values().stream()
                .collect(Collectors.toList());
    }

    /**
     * Applications for job offers owned by the logged-in employer.
     * If filterJobOfferId != null, we additionally filter by that job only.
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

        return appState.getAllApplications().values().stream()
                .filter(app -> {
                    JobOffer offer = appState.getOffer(app.getJobOfferId());
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

    // Translates JobOfferId -> Job Title
    public String getJobTitle(UUID offerId) {
        if (offerId == null) return "Unknown";
        JobOffer offer = appState.getOffer(offerId);
        return (offer != null) ? offer.getTitle() : "Offer Removed";
    }

    // Translates ApplicantId -> First Last Name
    public String getApplicantName(UUID applicantId) {
        if (applicantId == null) return "Unknown";
        Applicant applicant = appState.getApplicant(applicantId);
        return (applicant != null)
                ? applicant.getFirstName() + " " + applicant.getLastName()
                : "User Removed";
    }

    // in ApplicationBean

    public boolean isFilteredByJob() {
        return filterJobOfferId != null;
    }

    public String clearJobFilter() {
        this.filterJobOfferId = null;
        // stay on the same page
        return null;
    }

}
