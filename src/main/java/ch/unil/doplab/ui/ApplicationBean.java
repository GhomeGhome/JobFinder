package ch.unil.doplab.ui;

import ch.unil.doplab.Application;
import ch.unil.doplab.Applicant;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Named("applicationBean")
@RequestScoped
public class ApplicationBean {

    @Inject
    private ApplicationState appState;

    public List<Application> getAllApplications() {
        return appState.getAllApplications().values().stream()
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
        return (applicant != null) ? applicant.getFirstName() + " " + applicant.getLastName() : "User Removed";
    }
}
