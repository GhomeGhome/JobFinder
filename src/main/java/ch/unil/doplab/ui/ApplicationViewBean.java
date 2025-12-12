package ch.unil.doplab.ui;

import ch.unil.doplab.Application;
import ch.unil.doplab.Company;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.client.JobFinderClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Named
@ViewScoped
public class ApplicationViewBean implements Serializable {

    @Inject
    private JobFinderClient client;

    @Inject
    private LoginBean loginBean;

    private String jobId;      // URL param
    private JobOffer offer;
    private Company company;
    private boolean alreadyApplied;

    // Called by <f:viewAction>
    public void init() {
        if (jobId == null || jobId.isBlank()) {
            return;
        }

        UUID id = UUID.fromString(jobId);
        this.offer = client.getJobOffer(id);

        if (offer != null && offer.getCompanyId() != null) {
            this.company = client.getCompany(offer.getCompanyId());
        }

        // Check if logged applicant already applied
        if (loginBean.isApplicant() && loginBean.getLoggedApplicant() != null && offer != null) {
            UUID applicantId = loginBean.getLoggedApplicant().getId();
            List<Application> all = client.getAllApplications();
            this.alreadyApplied = all.stream()
                    .anyMatch(a ->
                            offer.getId().equals(a.getJobOfferId()) &&
                                    applicantId.equals(a.getApplicantId())
                    );
        }
    }

    public String apply() {
        if (!loginBean.isApplicant() || loginBean.getLoggedApplicant() == null || offer == null) {
            return null;
        }

        if (alreadyApplied) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Already applied",
                            "You have already applied to this job."));
            return null;
        }

        Application app = new Application();
        app.setJobOfferId(offer.getId());
        app.setApplicantId(loginBean.getLoggedApplicant().getId());
        // status & timestamps are set on server in addApplication()

        boolean ok = client.createApplication(app);

        if (ok) {
            alreadyApplied = true;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Application submitted",
                            "Your application has been sent."));
            // redirect to "My applications"
            return "/applicantApplications.xhtml?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "Could not submit application."));
            return null;
        }
    }

    // getters/setters

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public JobOffer getOffer() {
        return offer;
    }

    public Company getCompany() {
        return company;
    }

    public boolean isAlreadyApplied() {
        return alreadyApplied;
    }
}
