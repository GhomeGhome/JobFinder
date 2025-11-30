package ch.unil.doplab;

import java.util.Date;

public class Interview {

    private Long id;
    private JobOffer jobOffer;
    private Applicant applicant;
    private Date scheduledAt;
    private InterviewMode mode;
    private InterviewStatus status;
    private String locationOrLink;

    public Interview() {
    }

    public Interview(Long id,
                     JobOffer jobOffer,
                     Applicant applicant,
                     Date scheduledAt,
                     InterviewMode mode,
                     InterviewStatus status,
                     String locationOrLink) {
        this.id = id;
        this.jobOffer = jobOffer;
        this.applicant = applicant;
        this.scheduledAt = scheduledAt;
        this.mode = mode;
        this.status = status;
        this.locationOrLink = locationOrLink;
    }

    // getters & setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobOffer getJobOffer() {
        return jobOffer;
    }

    public void setJobOffer(JobOffer jobOffer) {
        this.jobOffer = jobOffer;
    }

    public Applicant getApplicant() {
        return applicant;
    }

    public void setApplicant(Applicant applicant) {
        this.applicant = applicant;
    }

    public Date getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Date scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public InterviewMode getMode() {
        return mode;
    }

    public void setMode(InterviewMode mode) {
        this.mode = mode;
    }

    public InterviewStatus getStatus() {
        return status;
    }

    public void setStatus(InterviewStatus status) {
        this.status = status;
    }

    public String getLocationOrLink() {
        return locationOrLink;
    }

    public void setLocationOrLink(String locationOrLink) {
        this.locationOrLink = locationOrLink;
    }
}
