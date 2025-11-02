package ch.unil.doplab.service.domain;

import ch.unil.doplab.JobOffer;
import ch.unil.doplab.JobOfferStatus;
import ch.unil.doplab.Applicant;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@ApplicationScoped
public class ApplicationState {

    /* ===================== JOB OFFERS ===================== */

    private Map<UUID, JobOffer> offers;
    private Map<UUID, String> employers; // employerId -> display name (optional)

    /* ===================== APPLICANTS ===================== */

    private Map<UUID, Applicant> applicants;

    @PostConstruct
    public void init() {
        offers = new TreeMap<>();
        employers = new TreeMap<>();
        applicants = new TreeMap<>();
        seed();
    }

    /* ===================== JOB OFFERS API ===================== */

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

        if (updated.getEmployerId() == null) {
            throw new IllegalArgumentException("JobOffer must have an employerId");
        }
        if (!employers.containsKey(updated.getEmployerId())) {
            throw new IllegalArgumentException("Unknown employerId: " + updated.getEmployerId());
        }

        updated.setId(id);
        if (updated.getCreatedAt() == null) {
            updated.setCreatedAt(current.getCreatedAt());
        }
        if (updated.getStatus() == null) {
            updated.setStatus(current.getStatus());
        }
        offers.put(id, updated);
        return true;
    }

    public boolean removeOffer(UUID id) { return offers.remove(id) != null; }
    public JobOffer getOffer(UUID id) { return offers.get(id); }
    public Map<UUID, JobOffer> getAllOffers() { return offers; }

    public JobOffer publishOffer(UUID offerId, UUID employerId) {
        var offer = offers.get(offerId);
        if (offer == null) throw new IllegalArgumentException("Offer not found");
        if (!offer.getEmployerId().equals(employerId)) {
            throw new IllegalArgumentException("Only the offer owner can publish this offer");
        }
        if (offer.getStatus() != JobOfferStatus.Draft) {
            throw new IllegalStateException("Only DRAFT offers can be published");
        }
        offer.setStatus(JobOfferStatus.Published);
        return offer;
    }

    /* ===================== APPLICANTS API ===================== */

    public Applicant addApplicant(Applicant applicant) {
        if (applicant.getId() == null) {
            applicant.setId(UUID.randomUUID());
        }
        applicants.put(applicant.getId(), applicant);
        return applicant;
    }

    public boolean setApplicant(UUID id, Applicant updated) {
        var current = applicants.get(id);
        if (current == null) return false;

        updated.setId(id);
        applicants.put(id, updated);
        return true;
    }

    public boolean removeApplicant(UUID id) { return applicants.remove(id) != null; }
    public Applicant getApplicant(UUID id) { return applicants.get(id); }
    public Map<UUID, Applicant> getAllApplicants() { return applicants; }

    /* ===================== SEED DATA ===================== */

    private void seed() {
        // Employers
        var annaEmployerId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        employers.put(annaEmployerId, "Anna Employer");

        // One seeded offer (DRAFT -> PUBLISHED)
        var offerId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
        var offer = new JobOffer(annaEmployerId, "Junior Java Developer");
        offer.setDescription("Build REST services with Jakarta EE.");
        offer.setEmploymentType("Full-time");
        offer.setCreatedAt(LocalDateTime.now());
        addOffer(offerId, offer);
        publishOffer(offerId, annaEmployerId);

        // Applicants
        var a1 = new Applicant("alice","secret","Alice","Martin",
                "alice@example.com","+41 79 111 22 33","Junior Java dev");
        a1.setCvInfo("https://example.com/cv/alice.pdf");
        addApplicant(a1);

        var a2 = new Applicant("bob","secret","Bob","Keller",
                "bob@example.com","@bob-on-telegram", null);
        addApplicant(a2);
    }
}
