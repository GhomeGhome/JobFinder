package ch.unil.doplab.ui;

import ch.unil.doplab.Applicant;
import ch.unil.doplab.Application; // Ensure this matches your DTO/Entity class
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.Company;
import ch.unil.doplab.client.JobFinderClient;
// import ch.unil.doplab.service.domain.ApplicationState; // REMOVED: No direct DB access

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable; // Good practice for Beans
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Named("applicantBean")
@RequestScoped
public class ApplicantBean implements Serializable {

    // @Inject
    // private ApplicationState appState; // OLD: Deleted

    @Inject
    private JobFinderClient client; // NEW: Use the REST Client

    @Inject
    private LoginBean loginBean; // NEW: Needed to get current Igor's ID

    public List<Applicant> getAllApplicants() {
        // return appState.getAllApplicants().values().stream()
        // .collect(Collectors.toList()); // OLD
        return client.getAllApplicants();
    }

    /**
     * NEW: Used by 'applicantApplications.xhtml'
     * Fetches all applications from server, then filters for the logged-in
     * applicant.
     */
    public List<Application> getMyApplications() {
        if (!loginBean.isApplicant() || loginBean.getLoggedApplicant() == null) {
            return Collections.emptyList();
        }

        UUID myId = loginBean.getLoggedApplicant().getId();

        // 1. Get ALL applications from the API
        List<Application> allApps = client.getAllApplications();

        // 2. Filter them in memory
        return allApps.stream()
                .filter(a -> myId.equals(a.getApplicantId()))
                .collect(Collectors.toList());
    }

    /**
     * NEW: Helper for the XHTML table to show Job Title
     */
    public String getJobTitle(UUID jobId) {
        if (jobId == null)
            return "Unknown";
        // JobOffer job = appState.getOffer(jobId); // OLD
        JobOffer job = client.getJobOffer(jobId); // NEW
        return (job != null) ? job.getTitle() : "Unknown Job";
    }

    /**
     * NEW: Helper for the XHTML table to show Company Name
     */
    public String getCompanyName(UUID jobId) {
        if (jobId == null)
            return "Unknown";

        // 1. Get the job to find the company ID
        JobOffer job = client.getJobOffer(jobId);
        if (job == null || job.getCompanyId() == null)
            return "Unknown Company";

        // 2. Get the company details
        Company comp = client.getCompany(job.getCompanyId());
        return (comp != null) ? comp.getName() : "Unknown Company";
    }

    /**
     * Helper to get Company ID for a job offer (for linking)
     */
    public UUID getCompanyId(UUID jobId) {
        if (jobId == null)
            return null;
        JobOffer job = client.getJobOffer(jobId);
        return (job != null) ? job.getCompanyId() : null;
    }

    public String updateStatus(ch.unil.doplab.Application app) {
        if (app != null) {
            // 1. Send the update to the server
            boolean success = client.updateApplication(app);

            if (success) {
                // 2. CRITICAL STEP: Refresh the local session data
                // We ask the server for the fresh Employer object (which includes the updated
                // application list)
                ch.unil.doplab.Employer freshEmployer = client.getEmployer(loginBean.getLoggedEmployer().getId());

                // 3. Update the LoginBean so the page shows the new data
                if (freshEmployer != null) {
                    loginBean.setLoggedEmployer(freshEmployer);
                }

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                                "Status updated to " + app.getStatus()));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not update status."));
            }
        }
        return null; // Reloads the page with the fresh data
    }

    public String statusLabel(ch.unil.doplab.ApplicationStatus st) {
        if (st == null)
            return "";
        return switch (st) {
            case In_review -> "In Review";
            case Rejected -> "Declined";
            default -> st.name();
        };
    }

    public String statusStyle(ch.unil.doplab.ApplicationStatus st) {
        if (st == null)
            return "";
        return switch (st) {
            case Rejected -> "background-color:#fee2e2; color:#b91c1c;";
            case Accepted -> "background-color:#dcfce7; color:#15803d;";
            case In_review -> "background-color:#fef9c3; color:#92400e;";
            case Submitted -> "background-color:#e5e7eb; color:#374151;";
            default -> "";
        };
    }

}