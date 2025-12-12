package ch.unil.doplab;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment Long
    private Long id;

    // Many interviews belong to one job offer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_offer_id", nullable = false)
    private JobOffer jobOffer;

    // Many interviews belong to one applicant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "scheduled_at", nullable = false)
    private Date scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", length = 20, nullable = false)
    private InterviewMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private InterviewStatus status;

    @Column(name = "location_or_link", length = 1000)
    private String locationOrLink;

    public Interview() {}

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
