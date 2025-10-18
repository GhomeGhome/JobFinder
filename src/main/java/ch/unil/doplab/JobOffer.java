package ch.unil.doplab;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

public class JobOffer {

    // === Attributs ===
    private UUID id;
    private UUID employerId;   // unique
    private UUID companyId; // optional (can be null)

    private String title;
    private String description;
    private String employmentType; // maybe enum later
    private JobOfferStatus status;      // Draft, Published, Closed

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;

    private List<String> requiredQualifications = new ArrayList<>();
    private List<String> requiredSkills = new ArrayList<>();

    // === Constructeur ===

    public JobOffer() {}

    public JobOffer(UUID id, UUID employerId, String title) {
        this.id = id;
        this.employerId = employerId;
        this.title = title;
        this.status = JobOfferStatus.Draft;
        this.createdAt = LocalDateTime.now();
}
    public JobOffer(UUID employerID, UUID companyId, String title, String description, String employmentType, LocalDate startDate, LocalDate endDate) {
        this.employerId = employerID;
        this.title = title;
        this.companyId = companyId;
        this.description = description;
        this.employmentType = employmentType;
        this.startDate = startDate;
        this.status = JobOfferStatus.Draft;
        this.endDate = endDate;
        this.createdAt = LocalDateTime.now();
    }

    public JobOffer(UUID employerId, String title) {
        this.employerId = employerId;
        this.title = title;
        this.status = JobOfferStatus.Draft;
        this.createdAt = LocalDateTime.now();
    }

    // === getters/setters (JSON-B requires these) ===
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
    public void setRequiredQualifications(List<String> requiredQualifications) {
        this.requiredQualifications = (requiredQualifications != null) ? requiredQualifications : new ArrayList<>();
    }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = (requiredSkills != null) ? requiredSkills : new ArrayList<>();
    }

    // === convenience mutators ===
    public void addRequiredSkill(String skill) {
        if (skill != null && !skill.isBlank()) requiredSkills.add(skill);
    }

    public void addRequiredQualification(String qualification) {
        if (qualification != null && !qualification.isBlank()) requiredQualifications.add(qualification);
    }

}
