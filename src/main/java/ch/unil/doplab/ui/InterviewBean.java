package ch.unil.doplab.ui;

import ch.unil.doplab.Applicant;
import ch.unil.doplab.Interview;
import ch.unil.doplab.InterviewMode;
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

    // in-memory "repository" for all interviews (simple for now)
    private static final List<Interview> INTERVIEWS = new ArrayList<>();
    private static long NEXT_ID = 1L;

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

    // ===== Getters/setters for form =====

    public String getSelectedJobOfferId() {
        return selectedJobOfferId;
    }

    public void setSelectedJobOfferId(String selectedJobOfferId) {
        this.selectedJobOfferId = selectedJobOfferId;
    }

    public String getSelectedApplicantId() {
        return selectedApplicantId;
    }

    public void setSelectedApplicantId(String selectedApplicantId) {
        this.selectedApplicantId = selectedApplicantId;
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
        return jobOfferBean.getAllOffers();
    }

    /**
     * Applicants shown in the "Applicant" dropdown.
     * For now: returns all applicants. You can later filter by selected job
     * (only those who applied to that specific job).
     */
    public List<Applicant> getApplicantsForSelectedJob() {
        // TODO later: filter by applications for selectedJobOfferId
        return applicantBean.getAllApplicants();
    }

    /**
     * Interviews visible in the employer table.
     * For now: returns all interviews as DTOs; you can later filter by employer.
     */
    public List<InterviewDTO> getEmployerInterviews() {
        List<InterviewDTO> result = new ArrayList<>();

        for (Interview interview : INTERVIEWS) {
            JobOffer job = interview.getJobOffer();
            Applicant app = interview.getApplicant();

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
                    interview.getLocationOrLink()
            ));
        }

        return result;
    }

    // ===== Actions =====

    public String schedule() {
        if (selectedJobOfferId == null || selectedApplicantId == null || scheduledAt == null) {
            // in a real app, use FacesMessage to show an error
            return null;
        }

        JobOffer selectedJob = findJobOfferById(selectedJobOfferId);
        Applicant selectedApplicant = findApplicantById(selectedApplicantId);


        if (selectedJob == null || selectedApplicant == null) {
            return null;
        }

        Interview interview = new Interview();
        interview.setId(NEXT_ID++);
        interview.setJobOffer(selectedJob);
        interview.setApplicant(selectedApplicant);
        interview.setScheduledAt(scheduledAt);

        InterviewMode interviewMode;
        try {
            interviewMode = InterviewMode.valueOf(
                    (mode != null) ? mode : "ONLINE"
            );
        } catch (IllegalArgumentException ex) {
            interviewMode = InterviewMode.ONLINE;
        }
        interview.setMode(interviewMode);

        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setLocationOrLink(locationOrLink);

        INTERVIEWS.add(interview);

        // success message
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Interview scheduled",
                        "Interview for " + selectedApplicant.getFirstName()
                                + " – " + selectedJob.getTitle() + " was scheduled.")
        );

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

    public String reschedule(Long id) {
        // For now: just mark as SCHEDULED again; you can implement a real reschedule flow later
        Interview interview = findInterviewById(id);
        if (interview != null) {
            interview.setStatus(InterviewStatus.SCHEDULED);
        }
        return null;
    }

    public String cancel(Long id) {
        Interview interview = findInterviewById(id);
        if (interview != null) {
            interview.setStatus(InterviewStatus.CANCELED);
        }
        return null;
    }

    // ===== Helpers =====

    private Interview findInterviewById(Long id) {
        for (Interview iv : INTERVIEWS) {
            if (iv.getId().equals(id)) {
                return iv;
            }
        }
        return null;
    }

    private JobOffer findJobOfferById(String id) {
        if (id == null) return null;

        for (JobOffer job : jobOfferBean.getAllOffers()) {
            if (job.getId() != null && job.getId().toString().equals(id)) {
                return job;
            }
        }
        return null;
    }

    private Applicant findApplicantById(String id) {
        if (id == null) return null;

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
        Object myId = loginBean.getLoggedApplicant().getId();

        for (Interview interview : INTERVIEWS) {
            // Check if this interview belongs to the current applicant
            if (interview.getApplicant() != null &&
                    interview.getApplicant().getId().equals(myId)) {

                JobOffer job = interview.getJobOffer();

                String jobTitle = (job != null) ? job.getTitle() : "";
                String companyName = (job != null) ? jobOfferBean.companyName(job) : "";
                // Applicant doesn't need to see their own name, they want Company name

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
                        interview.getLocationOrLink()
                ));
            }
        }
        return result;
    }
}
