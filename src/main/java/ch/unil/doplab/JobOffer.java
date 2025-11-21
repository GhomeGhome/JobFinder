package ch.unil.doplab;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Représente une offre d'emploi dans JobFinder.
 */
public class JobOffer {

    // ======================================================
    // ATTRIBUTS
    // ======================================================

    private UUID id;
    private UUID employerId;     // créateur de l'offre
    private UUID companyId;      // entreprise optionnelle

    private String title;
    private String description;
    private String employmentType;

    private JobOfferStatus status;      // Draft, Published, Closed, Reopened

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;

    private List<String> requiredQualifications = new ArrayList<>();
    private List<String> requiredSkills = new ArrayList<>();

    // Applications liées
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
    // APPLICATIONS RELIÉES
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
