package ch.unil.doplab;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente une candidature à une offre d'emploi.
 */
public class Application {

    // ======================================================
    // ATTRIBUTS
    // ======================================================

    private UUID id;
    private UUID jobOfferId;
    private UUID applicantId;

    private String cvUrl;                  // lien ou contenu du CV
    private LocalDateTime submittedAt;     // date de création
    private LocalDateTime updatedAt;       // dernière modification

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
    }

    public Application(UUID id, UUID jobOfferId, UUID applicantId,
                       String cvUrl, LocalDateTime submittedAt,
                       LocalDateTime updatedAt, ApplicationStatus status,
                       Double matchScore) {

        this.id = id;
        this.jobOfferId = jobOfferId;
        this.applicantId = applicantId;

        this.cvUrl = cvUrl;
        this.submittedAt = submittedAt != null ? submittedAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.submittedAt;
        this.status = status != null ? status : ApplicationStatus.Submitted;
        this.matchScore = matchScore;
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
