package ch.unil.doplab;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Représente une offre d'emploi dans JobFinder.
 */
@Entity
@Table(name = "job_offers")
public class JobOffer {

    // ======================================================
    // ATTRIBUTS
    // ======================================================

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "employer_id", nullable = false)
    private UUID employerId;     // créateur de l'offre

    @Column(name = "company_id")
    private UUID companyId;      // entreprise optionnelle

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(name = "employment_type", length = 100)
    private String employmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private JobOfferStatus status;      // Draft, Published, Closed, Reopened

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ---- lists of simple values -> ElementCollection ----
    @ElementCollection
    @CollectionTable(
            name = "job_offer_required_qualifications",
            joinColumns = @JoinColumn(name = "job_offer_id")
    )
    @Column(name = "qualification", length = 255)
    private List<String> requiredQualifications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "job_offer_required_skills",
            joinColumns = @JoinColumn(name = "job_offer_id")
    )
    @Column(name = "skill", length = 255)
    private List<String> requiredSkills = new ArrayList<>();

    // Applications liées – on laisse ça côté logique, pas en DB
    @Transient
    private List<UUID> applicationIds = new ArrayList<>();


    // ======================================================
    // CONSTRUCTEURS
    // ======================================================

    public JobOffer() {}

    public JobOffer(UUID id, UUID employerId, UUID companyId,
                    String title, String description, String employmentType,
                    LocalDate startDate, LocalDate endDate) {

        this.id = id;
        this.employerId = employerId;
        this.companyId = companyId;
        this.title = title;
        this.description = description;
        this.employmentType = employmentType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = JobOfferStatus.Draft;
        this.createdAt = LocalDateTime.now();
    }

    public JobOffer(UUID employerId, UUID companyId,
                    String title, String description, String employmentType,
                    LocalDate startDate, LocalDate endDate) {
        this(null, employerId, companyId, title, description, employmentType,
                startDate, endDate);
    }

    // auto-defaults if JPA creates it
    @PrePersist
    private void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = JobOfferStatus.Draft;
        }
    }

    // ======================================================
    // GETTERS / SETTERS
    // ======================================================

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEmployerId() { return employerId; }
    public void setEmployerId(UUID employerId) { this.employerId = employerId; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public JobOfferStatus getStatus() { return status; }
    public void setStatus(JobOfferStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getRequiredQualifications() { return requiredQualifications; }
    public void setRequiredQualifications(List<String> list) {
        this.requiredQualifications = (list != null) ? list : new ArrayList<>();
    }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> list) {
        this.requiredSkills = (list != null) ? list : new ArrayList<>();
    }

    // ======================================================
    // APPLICATIONS RELIÉES (LOGIQUE SEULEMENT)
    // ======================================================

    public List<UUID> getApplicationIds() { return applicationIds; }

    public void addApplicationId(UUID id) {
        if (id != null && !applicationIds.contains(id))
            applicationIds.add(id);
    }

    public void removeApplicationId(UUID id) {
        applicationIds.remove(id);
    }

    // ======================================================
    // UTILS
    // ======================================================

    public void addRequiredSkill(String skill) {
        if (skill != null && !skill.isBlank()) requiredSkills.add(skill);
    }

    public void addRequiredQualification(String q) {
        if (q != null && !q.isBlank()) requiredQualifications.add(q);
    }

    @Override
    public String toString() {
        return "JobOffer{id=%s, title=%s, employer=%s, status=%s}"
                .formatted(id, title, employerId, status);
    }
}
