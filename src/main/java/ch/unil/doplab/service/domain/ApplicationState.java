package ch.unil.doplab.service.domain;

import ch.unil.doplab.JobOffer;
import ch.unil.doplab.JobOfferStatus;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@ApplicationScoped
public class ApplicationState {

    private Map<UUID, JobOffer> offers;
    // Minimal validation support: keep a small set of known employer IDs.
    // (You can replace this with a real Employer map later.)
    private Map<UUID, String> employers; // id -> display name (optional)

    @PostConstruct
    public void init() {
        offers = new TreeMap<>();
        employers = new TreeMap<>();
        seed();
    }

    /* ===================== JOB OFFERS ===================== */

    public JobOffer addOffer(JobOffer offer) {
        if (offer.getId() != null) {
            return addOffer(offer.getId(), offer);
        }
        return addOffer(UUID.randomUUID(), offer);
    }

    public JobOffer addOffer(UUID id, JobOffer offer) {
        if (offer.getEmployerId() == null) {
            throw new IllegalArgumentException("JobOffer must have an employerId");
        }
        if (!employers.containsKey(offer.getEmployerId())) {
            throw new IllegalArgumentException("Unknown employerId: " + offer.getEmployerId());
        }
        // Defaults
        if (offer.getStatus() == null) {
            offer.setStatus(JobOfferStatus.Draft);
        }
        if (offer.getCreatedAt() == null) {
            offer.setCreatedAt(LocalDateTime.now());
        }
        offer.setId(id);
        offers.put(id, offer);
        return offer;
    }

    public boolean setOffer(UUID id, JobOffer updated) {
        var current = offers.get(id);
        if (current == null) return false;

        // If employerId changes, still validate it
        if (updated.getEmployerId() == null) {
            throw new IllegalArgumentException("JobOffer must have an employerId");
        }
        if (!employers.containsKey(updated.getEmployerId())) {
            throw new IllegalArgumentException("Unknown employerId: " + updated.getEmployerId());
        }

        updated.setId(id);
        // Keep createdAt if not provided
        if (updated.getCreatedAt() == null) {
            updated.setCreatedAt(current.getCreatedAt());
        }
        // Keep status if not provided
        if (updated.getStatus() == null) {
            updated.setStatus(current.getStatus());
        }
        offers.put(id, updated);
        return true;
    }

    public boolean removeOffer(UUID id) {
        return offers.remove(id) != null;
    }

    public JobOffer getOffer(UUID id) {
        return offers.get(id);
    }

    public Map<UUID, JobOffer> getAllOffers() {
        return offers;
    }

    public JobOffer publishOffer(UUID offerId, UUID employerId) {
        var offer = offers.get(offerId);
        if (offer == null) {
            throw new IllegalArgumentException("Offer not found");
        }
        if (!offer.getEmployerId().equals(employerId)) {
            throw new IllegalArgumentException("Only the offer owner can publish this offer");
        }
        if (offer.getStatus() != JobOfferStatus.Draft) {
            throw new IllegalStateException("Only DRAFT offers can be published");
        }
        offer.setStatus(JobOfferStatus.Published);
        return offer;
    }

    /* ===================== SEED DATA ===================== */

    private void seed() {
        // Seed one employer so validation passes
        var annaEmployerId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        employers.put(annaEmployerId, "Anna Employer");

        // Seed one offer (DRAFT → then publish for quick testing)
        var offerId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
        var offer = new JobOffer(annaEmployerId, "Junior Java Developer");
        offer.setDescription("Build REST services with Jakarta EE.");
        offer.setEmploymentType("Full-time");
        offer.setCreatedAt(LocalDateTime.now());
        addOffer(offerId, offer);

        // Make it visible
        publishOffer(offerId, annaEmployerId);
    }
}
