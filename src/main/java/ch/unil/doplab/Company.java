package ch.unil.doplab;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Représente une entreprise dans JobFinder.
 */
@Entity
@Table(name = "companies")
public class Company {

    // ======================================================
    // ATTRIBUTS
    // ======================================================

    @Id
    private UUID id;
    private UUID ownerEmployerId;     // Le fondateur ou administrateur principal

    @Column(nullable = false)
    private String name;
    private String location;

    @Lob
    private String description;

    // For now we **do not persist** these lists – we can always add proper
    // relations later if needed. They are only used in memory.
    @Transient
    private List<UUID> employerIds = new ArrayList<>();   // Tous les employeurs liés
    @Transient
    private List<UUID> jobOfferIds = new ArrayList<>();   // Toutes les offres publiées


    // ======================================================
    // CONSTRUCTEURS
    // ======================================================

    public Company() {}

    public Company(UUID id, UUID ownerEmployerId, String name,
                   String location, String description) {

        this.id = id;
        this.ownerEmployerId = ownerEmployerId;
        this.name = name;
        this.location = location;
        this.description = description;
    }

    public Company(UUID ownerEmployerId, String name,
                   String location, String description) {

        this(null, ownerEmployerId, name, location, description);
    }

    // Generate an id automatically if it's null when persisting
    @PrePersist
    public void ensureId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    // ======================================================
    // GETTERS / SETTERS
    // ======================================================

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOwnerEmployerId() { return ownerEmployerId; }
    public void setOwnerEmployerId(UUID id) { this.ownerEmployerId = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    // ======================================================
    // RELATIONS EMPLOYERS (ONLY IN MEMORY FOR NOW)
    // ======================================================

    public List<UUID> getEmployerIds() { return employerIds; }

    public void addEmployerId(UUID id) {
        if (id != null && !employerIds.contains(id))
            employerIds.add(id);
    }

    public void removeEmployerId(UUID id) {
        employerIds.remove(id);
    }


    // ======================================================
    // RELATIONS JOB OFFERS (IN MEMORY FOR NOW ONLY)
    // ======================================================

    public List<UUID> getJobOfferIds() { return jobOfferIds; }

    public void addJobOfferId(UUID id) {
        if (id != null && !jobOfferIds.contains(id))
            jobOfferIds.add(id);
    }

    public void removeJobOfferId(UUID id) {
        jobOfferIds.remove(id);
    }


    // ======================================================
    // OVERRIDE
    // ======================================================

    @Override
    public String toString() {
        return "Company{id=%s, name=%s, employers=%d, offers=%d}"
                .formatted(id, name, employerIds.size(), jobOfferIds.size());
    }
}
