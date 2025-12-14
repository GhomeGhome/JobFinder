package ch.unil.doplab.ui;

import ch.unil.doplab.Company;
import ch.unil.doplab.Employer;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.JobOfferStatus;
import ch.unil.doplab.client.JobFinderClient;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Named("employerJobFormBean")
@SessionScoped
public class EmployerJobFormBean implements Serializable {

    @Inject
    private JobFinderClient client;

    @Inject
    private LoginBean loginBean;

    private JobOffer jobOffer = new JobOffer();
    private List<Company> availableCompanies;
    private String selectedCompanyId; // binds to <selectOneMenu>
    private String statusString = "Draft"; // default
    private String requiredSkillInput; // chip input for required skills

    @PostConstruct
    public void init() {
        resetForm();
    }

    public void resetForm() {
        // Reset form fields for a new job
        jobOffer = new JobOffer();
        selectedCompanyId = null;
        statusString = "Draft";
        requiredSkillInput = null;

        // Ensure we are logged as employer
        if (!loginBean.isEmployer() || loginBean.getLoggedEmployer() == null) {
            return;
        }

        Employer emp = loginBean.getLoggedEmployer();
        UUID empId = emp.getId();

        // Load companies owned by OR linked to this employer
        List<Company> all = client.getAllCompanies();
        UUID worksAt = emp.getCompanyId();
        availableCompanies = all.stream()
                .filter(c -> empId.equals(c.getOwnerEmployerId())
                        || (worksAt != null && worksAt.equals(c.getId())))
                .collect(Collectors.toList());
    }

    public String save() {
        if (!loginBean.isEmployer() || loginBean.getLoggedEmployer() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "You must be logged in as an employer.", null));
            return null;
        }

        Employer emp = loginBean.getLoggedEmployer();

        // set employer on offer
        jobOffer.setEmployerId(emp.getId());

        // set company if selected
        if (selectedCompanyId != null && !selectedCompanyId.isBlank()) {
            jobOffer.setCompanyId(UUID.fromString(selectedCompanyId));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Choose a company",
                            "Every job post must belong to a company. Please select one from the list before creating the post."));
            return null;
        }

        // set status from dropdown
        try {
            JobOfferStatus st = JobOfferStatus.valueOf(statusString);
            jobOffer.setStatus(st);
        } catch (IllegalArgumentException ex) {
            jobOffer.setStatus(JobOfferStatus.Draft);
        }

        // Call REST
        JobOffer created = client.createJobOffer(jobOffer);
        if (created == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not create job offer.", null));
            return null;
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Job offer created successfully.", null));

        // Redirect to employer job posts page
        return "/employerJobOffers.xhtml?faces-redirect=true";
    }

    // ---- Required skills helpers ----
    public void addRequiredSkill() {
        if (jobOffer.getRequiredSkills() == null) {
            jobOffer.setRequiredSkills(new java.util.ArrayList<>());
        }
        if (requiredSkillInput == null || requiredSkillInput.isBlank()) {
            return;
        }
        // Support comma-separated input; trim, de-duplicate, ignore blanks
        java.util.LinkedHashSet<String> toAdd = new java.util.LinkedHashSet<>();
        for (String s : requiredSkillInput.split(",")) {
            String t = s == null ? "" : s.trim();
            if (!t.isBlank())
                toAdd.add(t);
        }
        for (String s : toAdd) {
            if (!jobOffer.getRequiredSkills().contains(s)) {
                jobOffer.getRequiredSkills().add(s);
            }
        }
        requiredSkillInput = null; // clear input after add
    }

    public void removeRequiredSkill(String skill) {
        if (skill == null)
            return;
        if (jobOffer.getRequiredSkills() == null)
            return;
        jobOffer.getRequiredSkills().removeIf(s -> skill.equalsIgnoreCase(s));
    }

    // ========== Getters / setters ==========

    public JobOffer getJobOffer() {
        return jobOffer;
    }

    public void setJobOffer(JobOffer jobOffer) {
        this.jobOffer = jobOffer;
    }

    public List<Company> getAvailableCompanies() {
        return availableCompanies;
    }

    public String getSelectedCompanyId() {
        return selectedCompanyId;
    }

    public void setSelectedCompanyId(String selectedCompanyId) {
        this.selectedCompanyId = selectedCompanyId;
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    public String getRequiredSkillInput() {
        return requiredSkillInput;
    }

    public void setRequiredSkillInput(String requiredSkillInput) {
        this.requiredSkillInput = requiredSkillInput;
    }
}
