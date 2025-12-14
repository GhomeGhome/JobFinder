package ch.unil.doplab.ui;

import ch.unil.doplab.Applicant;
import ch.unil.doplab.Interview;
import ch.unil.doplab.InterviewStatus;
import ch.unil.doplab.JobOffer;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

@Named
@SessionScoped
public class InterviewBean implements Serializable {

    // === form fields ===
    private String selectedJobOfferId;
    private String selectedApplicantId;
    private Date scheduledAt;
    private String mode; // "ONLINE", "ONSITE", "PHONE"
    private String locationOrLink;

    // you already have these UI beans in your app
    @Inject
    private JobOfferBean jobOfferBean;

    @Inject
    private ApplicantBean applicantBean;

    @Inject
    private LoginBean loginBean;

    @Inject
    private ch.unil.doplab.client.JobFinderClient client;

    // ===== Getters/setters for form =====

    public String getSelectedJobOfferId() {
        return selectedJobOfferId;
    }

    public void setSelectedJobOfferId(String selectedJobOfferId) {
        this.selectedJobOfferId = (selectedJobOfferId != null) ? selectedJobOfferId.trim() : null;
    }

    public String getSelectedApplicantId() {
        return selectedApplicantId;
    }

    public void setSelectedApplicantId(String selectedApplicantId) {
        this.selectedApplicantId = (selectedApplicantId != null) ? selectedApplicantId.trim() : null;
    }

    public Date getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Date scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getLocationOrLink() {
        return locationOrLink;
    }

    public void setLocationOrLink(String locationOrLink) {
        this.locationOrLink = locationOrLink;
    }

    // ===== Data for dropdowns & table =====

    /**
     * Job posts that the employer can choose from when scheduling.
     * For now: just reuse all offers; you can later filter by logged-in employer.
     */
    public List<JobOffer> getEmployerOffers() {
        // Show only offers owned by logged-in employer
        return jobOfferBean.getEmployerOffers();
    }

    /**
     * Applicants shown in the "Applicant" dropdown.
     * For now: returns all applicants. You can later filter by selected job
     * (only those who applied to that specific job).
     */
    public List<Applicant> getApplicantsForSelectedJob() {
        List<Applicant> result = new ArrayList<>();

        java.util.LinkedHashSet<java.util.UUID> eligibleIds = new java.util.LinkedHashSet<>();
        
        // Statuses that are eligible for scheduling interviews
        java.util.Set<String> eligibleStatuses = java.util.Set.of("Submitted", "In_review", "Accepted");

        // If no job is selected, show all eligible applicants across all offers
        if (selectedJobOfferId == null || selectedJobOfferId.isBlank()) {
            for (JobOffer offer : getEmployerOffers()) {
                if (offer == null || offer.getId() == null)
                    continue;
                var apps = client.getApplicationsByOffer(offer.getId());
                for (ch.unil.doplab.Application a : apps) {
                    if (a.getStatus() != null && eligibleStatuses.contains(a.getStatus().name())) {
                        eligibleIds.add(a.getApplicantId());
                    }
                }
            }
        }
        // If a job is selected, show eligible applicants for that specific job
        else {
            java.util.UUID jobId;
            try {
                jobId = java.util.UUID.fromString(selectedJobOfferId);
            } catch (IllegalArgumentException e) {
                return result;
            }

            var apps = client.getApplicationsByOffer(jobId);
            for (ch.unil.doplab.Application a : apps) {
                if (a.getStatus() != null && eligibleStatuses.contains(a.getStatus().name())) {
                    eligibleIds.add(a.getApplicantId());
                }
            }
        }

        for (java.util.UUID aid : eligibleIds) {
            Applicant a = client.getApplicant(aid);
            if (a != null)
                result.add(a);
        }
        return result;
    }

    /**
     * Interviews visible in the employer table.
     * For now: returns all interviews as DTOs; you can later filter by employer.
     */
    public List<InterviewDTO> getEmployerInterviews() {
        List<InterviewDTO> result = new ArrayList<>();

        if (!loginBean.isEmployer() || loginBean.getLoggedEmployer() == null) {
            return result;
        }

        java.util.UUID employerId = loginBean.getLoggedEmployer().getId();
        if (employerId == null) {
            return result;
        }

        List<Interview> interviews = client.getInterviewsByEmployer(employerId);

        java.util.Map<java.util.UUID, JobOffer> offerCache = new java.util.HashMap<>();
        java.util.Map<java.util.UUID, Applicant> applicantCache = new java.util.HashMap<>();

        for (Interview interview : interviews) {
            java.util.UUID jobId = interview.getJobOfferId();
            java.util.UUID applicantId = interview.getApplicantId();

            JobOffer job = null;
            if (jobId != null) {
                job = offerCache.computeIfAbsent(jobId, client::getJobOffer);
            }

            Applicant app = null;
            if (applicantId != null) {
                app = applicantCache.computeIfAbsent(applicantId, client::getApplicant);
            }

            String jobTitle = (job != null) ? job.getTitle() : "";
            String companyName = (job != null) ? jobOfferBean.companyName(job) : "";
            String applicantName = (app != null)
                    ? app.getFirstName() + " " + app.getLastName()
                    : "";

            String modeLabel = (interview.getMode() != null)
                    ? interview.getMode().name()
                    : "";
            String statusLabel = (interview.getStatus() != null)
                    ? interview.getStatus().name()
                    : "";

            result.add(new InterviewDTO(
                    interview.getId(),
                    jobTitle,
                    companyName,
                    applicantName,
                    interview.getScheduledAt(),
                    modeLabel,
                    statusLabel,
                    interview.getLocationOrLink()));
        }

        return result;
    }

    // ===== Actions =====

    public String schedule() {
        if (selectedJobOfferId == null || selectedApplicantId == null || scheduledAt == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Missing data", "Select job, applicant and date."));
            return null;
        }

        JobOffer selectedJob = findJobOfferById(selectedJobOfferId);
        Applicant selectedApplicant = findApplicantById(selectedApplicantId);

        if (selectedJob == null || selectedApplicant == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not found", "Job or applicant not found."));
            return null;
        }

        // Verify applicant has applied to this job (any eligible status)
        java.util.UUID jobId = java.util.UUID.fromString(selectedJobOfferId);
        java.util.UUID appId = java.util.UUID.fromString(selectedApplicantId);
        java.util.Set<String> eligibleStatuses = java.util.Set.of("Submitted", "In_review", "Accepted");
        boolean eligible = false;
        for (ch.unil.doplab.Application a : client.getApplicationsByOffer(jobId)) {
            if (appId.equals(a.getApplicantId()) && a.getStatus() != null && eligibleStatuses.contains(a.getStatus().name())) {
                eligible = true;
                break;
            }
        }
        if (!eligible) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not allowed",
                            "This applicant has not applied to this job or was rejected."));
            return null;
        }

        Interview created;
        try {
            created = client.createInterview(jobId, appId, scheduledAt, mode, locationOrLink);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Could not persist interview."));
            return null;
        }

        if (created == null || created.getId() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Could not persist interview."));
            return null;
        }

        // success message
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Interview scheduled",
                        "Interview for " + selectedApplicant.getFirstName()
                                + " – " + selectedJob.getTitle() + " was scheduled."));

        // clear form
        selectedJobOfferId = null;
        selectedApplicantId = null;
        scheduledAt = null;
        mode = null;
        locationOrLink = null;

        return null; // stay on same page
    }

    public String view(Long id) {
        // For now, just a placeholder – you can navigate to a detail page later
        // e.g. store the id and redirect to employerInterviewDetail.xhtml
        return null;
    }

    public String reschedule(java.util.UUID id) {
        // For now: just mark as SCHEDULED again; you can implement a real reschedule
        // flow later
        if (id != null) {
            try {
                client.updateInterviewStatus(id, InterviewStatus.SCHEDULED.name());
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public String cancel(java.util.UUID id) {
        if (id != null) {
            try {
                client.updateInterviewStatus(id, InterviewStatus.CANCELED.name());
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private JobOffer findJobOfferById(String id) {
        if (id == null)
            return null;

        for (JobOffer job : jobOfferBean.getAllOffers()) {
            if (job.getId() != null && job.getId().toString().equals(id)) {
                return job;
            }
        }
        return null;
    }

    private Applicant findApplicantById(String id) {
        if (id == null)
            return null;

        for (Applicant app : applicantBean.getAllApplicants()) {
            if (app.getId() != null && app.getId().toString().equals(id)) {
                return app;
            }
        }
        return null;
    }

    /**
     * NEW: Returns interviews for the logged-in applicant.
     */
    public List<InterviewDTO> getApplicantInterviews() {
        List<InterviewDTO> result = new ArrayList<>();

        // 1. Get current applicant ID
        if (!loginBean.isApplicant() || loginBean.getLoggedApplicant() == null) {
            return result;
        }
        // Assuming your Applicant entity uses UUID or Long.
        // Based on your InterviewBean, it seems we match objects directly or by ID.
        // Let's use the ID for safety.

        java.util.UUID myId = loginBean.getLoggedApplicant().getId();
        if (myId == null) {
            return result;
        }

        List<Interview> interviews = client.getInterviewsByApplicant(myId);
        java.util.Map<java.util.UUID, JobOffer> offerCache = new java.util.HashMap<>();

        for (Interview interview : interviews) {
            java.util.UUID jobId = interview.getJobOfferId();
            JobOffer job = null;
            if (jobId != null) {
                job = offerCache.computeIfAbsent(jobId, client::getJobOffer);
            }

            String jobTitle = (job != null) ? job.getTitle() : "";
            String companyName = (job != null) ? jobOfferBean.companyName(job) : "";
            String modeLabel = (interview.getMode() != null) ? interview.getMode().name() : "";
            String statusLabel = (interview.getStatus() != null) ? interview.getStatus().name() : "";

            result.add(new InterviewDTO(
                    interview.getId(),
                    jobTitle,
                    companyName,
                    "", // Applicant name not needed for their own view
                    interview.getScheduledAt(),
                    modeLabel,
                    statusLabel,
                    interview.getLocationOrLink()));
        }
        return result;
    }
}
