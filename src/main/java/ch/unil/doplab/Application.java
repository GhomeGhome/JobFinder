package ch.unil.doplab;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;

/**
 * Représente une candidature à une offre d'emploi.
 */
@Entity
@Table(name="applications")
public class Application {

    // ======================================================
    // ATTRIBUTS
    // ======================================================

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "job_offer_id")
    private UUID jobOfferId;

    @Column(name = "applicant_id")
    private UUID applicantId;

    private String cvUrl;                  // lien ou contenu du CV

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;     // date de création

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;       // dernière modification

    @Column(name = "created_at")
    private LocalDateTime createdAt;       // première création (audit)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ApplicationStatus status;      // Submitted → In_review → Rejected/Accepted/Withdrawn

    private Double matchScore;             // score éventuel pour le matching


    // ======================================================
    // CONSTRUCTEURS
    // ======================================================

    public Application() {}

    public Application(UUID id, UUID jobOfferId, UUID applicantId) {
        this.id = id;
        this.jobOfferId = jobOfferId;
        this.applicantId = applicantId;

        this.status = ApplicationStatus.Submitted;
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = this.submittedAt;
        // createdAt will be set in @PrePersist if null
    }

    public Application(UUID id,
                       UUID jobOfferId,
                       UUID applicantId,
                       String cvUrl,
                       LocalDateTime submittedAt,
                       LocalDateTime updatedAt,
                       ApplicationStatus status,
                       Double matchScore) {

        this.id = id;
        this.jobOfferId = jobOfferId;
        this.applicantId = applicantId;

        this.cvUrl = cvUrl;
        this.submittedAt = (submittedAt != null) ? submittedAt : LocalDateTime.now();
        this.updatedAt = (updatedAt != null) ? updatedAt : this.submittedAt;
        this.status = (status != null) ? status : ApplicationStatus.Submitted;
        this.matchScore = matchScore;
    }


    // ======================================================
    // JPA LIFECYCLE
    // ======================================================

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (submittedAt == null) {
            submittedAt = now;
        }
        if (updatedAt == null) {
            updatedAt = submittedAt;
        }
        if (status == null) {
            status = ApplicationStatus.Submitted;
        }
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    // ======================================================
    // GETTERS / SETTERS
    // ======================================================

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getJobOfferId() { return jobOfferId; }
    public void setJobOfferId(UUID jobOfferId) { this.jobOfferId = jobOfferId; }

    public UUID getApplicantId() { return applicantId; }
    public void setApplicantId(UUID applicantId) { this.applicantId = applicantId; }

    public String getCvUrl() { return cvUrl; }
    public void setCvUrl(String cvUrl) { this.cvUrl = cvUrl; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }


    // ======================================================
    // MÉTHODES UTILITAIRES
    // ======================================================

    public void markUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Application{id=%s, jobOfferId=%s, applicantId=%s, status=%s}"
                .formatted(id, jobOfferId, applicantId, status);
    }
}
