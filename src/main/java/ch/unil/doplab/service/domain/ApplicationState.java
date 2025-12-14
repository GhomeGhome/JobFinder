package ch.unil.doplab.service.domain;

import ch.unil.doplab.*;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central in-memory state + DB persistence (StudyBuddy style):
 * - DB = persistent store
 * - Maps = runtime cache
 * - All logic stays here (resources stay thin)
 */
@ApplicationScoped
public class ApplicationState {

    @PersistenceContext(unitName = "jobfinderPU")
    private EntityManager em;

    private Map<UUID, Employer> employers;
    private Map<UUID, Applicant> applicants;
    private Map<UUID, Company> companies;
    private Map<UUID, JobOffer> jobOffers;
    private Map<UUID, Application> applications;
    private Map<UUID, Interview> interviews;

    // ======================================================
    // INIT / LOAD
    // ======================================================

    @PostConstruct
    public void init() {
        employers = new HashMap<>();
        applicants = new HashMap<>();
        companies = new HashMap<>();
        jobOffers = new HashMap<>();
        applications = new HashMap<>();
        interviews = new HashMap<>();

        loadFromDatabase();
    }

    /**
     * Loads all rows from DB into RAM caches and rebuilds inverse lists.
     */
    private void loadFromDatabase() {
        clearObjects();

        for (Employer e : em.createQuery("SELECT e FROM Employer e", Employer.class).getResultList()) {
            employers.put(e.getId(), e);
        }
        for (Applicant a : em.createQuery(
                "SELECT DISTINCT a FROM Applicant a LEFT JOIN FETCH a.skills", Applicant.class).getResultList()) {
            applicants.put(a.getId(), a);
        }
        for (Company c : em.createQuery("SELECT c FROM Company c", Company.class).getResultList()) {
            companies.put(c.getId(), c);
        }
        for (JobOffer o : em.createQuery("SELECT o FROM JobOffer o", JobOffer.class).getResultList()) {
            jobOffers.put(o.getId(), o);
        }
        for (Application app : em.createQuery("SELECT a FROM Application a", Application.class).getResultList()) {
            applications.put(app.getId(), app);
        }

        // Wrap interview loading in try/catch - table may not exist yet
        try {
            for (Interview iv : em.createQuery("SELECT i FROM Interview i", Interview.class).getResultList()) {
                if (iv.getId() != null) {
                    interviews.put(iv.getId(), iv);
                }
            }
        } catch (Exception ignored) {
            // interviews table doesn't exist yet - that's OK
        }

        rebuildInverseRelations(); // ✅ this was missing
    }

    /**
     * Rebuilds "inverse" lists in RAM only (jobOfferIds, applicationIds,
     * employerIds, etc.)
     * This mirrors what StudyBuddy did.
     */
    private void rebuildInverseRelations() {

        for (Employer e : employers.values()) {
            if (e.getJobOfferIds() != null)
                e.getJobOfferIds().clear();
        }
        for (Company c : companies.values()) {
            if (c.getEmployerIds() != null)
                c.getEmployerIds().clear();
            if (c.getJobOfferIds() != null)
                c.getJobOfferIds().clear();
        }
        for (Applicant a : applicants.values()) {
            if (a.getApplicationIds() != null)
                a.getApplicationIds().clear();
        }
        for (JobOffer o : jobOffers.values()) {
            if (o.getApplicationIds() != null)
                o.getApplicationIds().clear();
        }

        // Link JobOffer -> Employer and Company
        for (JobOffer o : jobOffers.values()) {
            UUID empId = o.getEmployerId();
            if (empId != null) {
                Employer e = employers.get(empId);
                if (e != null)
                    e.addJobOfferId(o.getId());
            }

            UUID compId = o.getCompanyId();
            if (compId != null) {
                Company c = companies.get(compId);
                if (c != null)
                    c.addJobOfferId(o.getId());
            }
        }

        // Link Application -> JobOffer and Applicant
        for (Application a : applications.values()) {
            JobOffer offer = jobOffers.get(a.getJobOfferId());
            if (offer != null)
                offer.addApplicationId(a.getId());

            Applicant ap = applicants.get(a.getApplicantId());
            if (ap != null)
                ap.addApplicationId(a.getId());
        }

        // Link Company <-> owner Employer
        for (Company c : companies.values()) {
            UUID ownerId = c.getOwnerEmployerId();
            if (ownerId != null) {
                Employer owner = employers.get(ownerId);
                if (owner != null) {
                    owner.setCompanyId(c.getId());
                    c.addEmployerId(ownerId);
                }
            }
        }
    }

    private void clearObjects() {
        employers.clear();
        applicants.clear();
        companies.clear();
        jobOffers.clear();
        applications.clear();
        interviews.clear();
    }

    // ======================================================
    // INTERVIEWS (DB + cache)
    // ======================================================

    public List<Interview> listInterviews() {
        return new ArrayList<>(interviews.values());
    }

    public List<Interview> listInterviewsByApplicantId(UUID applicantId) {
        List<Interview> list = em.createQuery(
                "SELECT i FROM Interview i WHERE i.applicantId = :applicantId", Interview.class)
                .setParameter("applicantId", applicantId)
                .getResultList();

        for (Interview i : list) {
            if (i.getId() != null) {
                interviews.put(i.getId(), i);
            }
        }
        return list;
    }

    public List<Interview> listInterviewsByEmployerId(UUID employerId) {
        List<UUID> offerIds = em.createQuery(
                "SELECT o.id FROM JobOffer o WHERE o.employerId = :employerId", UUID.class)
                .setParameter("employerId", employerId)
                .getResultList();

        if (offerIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Interview> list = em.createQuery(
                "SELECT i FROM Interview i WHERE i.jobOfferId IN :offerIds", Interview.class)
                .setParameter("offerIds", offerIds)
                .getResultList();

        for (Interview i : list) {
            if (i.getId() != null) {
                interviews.put(i.getId(), i);
            }
        }
        return list;
    }

    @Transactional
    public Interview createInterview(UUID jobOfferId,
            UUID applicantId,
            java.util.Date scheduledAt,
            String modeRaw,
            String locationOrLink) {
        if (jobOfferId == null || applicantId == null || scheduledAt == null) {
            throw new IllegalArgumentException("jobOfferId, applicantId and scheduledAt are required");
        }

        // Validate references exist
        JobOffer offer = em.find(JobOffer.class, jobOfferId);
        if (offer == null) {
            throw new IllegalArgumentException("Unknown JobOffer: " + jobOfferId);
        }
        Applicant applicant = em.find(Applicant.class, applicantId);
        if (applicant == null) {
            throw new IllegalArgumentException("Unknown Applicant: " + applicantId);
        }

        InterviewMode mode;
        try {
            mode = (modeRaw == null || modeRaw.isBlank()) ? InterviewMode.ONLINE : InterviewMode.valueOf(modeRaw);
        } catch (IllegalArgumentException ex) {
            mode = InterviewMode.ONLINE;
        }

        Interview iv = new Interview();
        iv.setId(UUID.randomUUID()); // Generate UUID before persist
        iv.setJobOfferId(jobOfferId);
        iv.setApplicantId(applicantId);
        iv.setScheduledAt(scheduledAt);
        iv.setMode(mode);
        iv.setStatus(InterviewStatus.SCHEDULED);
        iv.setLocationOrLink(locationOrLink);

        em.persist(iv);
        if (iv.getId() != null) {
            interviews.put(iv.getId(), iv);
        }
        return iv;
    }

    @Transactional
    public Interview updateInterviewStatus(UUID id, InterviewStatus status) {
        if (id == null || status == null) {
            throw new IllegalArgumentException("id and status are required");
        }

        Interview iv = em.find(Interview.class, id);
        if (iv == null) {
            return null;
        }

        iv.setStatus(status);
        interviews.put(id, iv);
        return iv;
    }

    public Interview getInterview(UUID id) {
        if (id == null)
            return null;
        Interview iv = interviews.get(id);
        if (iv == null) {
            iv = em.find(Interview.class, id);
            if (iv != null)
                interviews.put(id, iv);
        }
        return iv;
    }

    @Transactional
    public Interview rescheduleInterview(UUID id, java.util.Date newDate, String newMode) {
        if (id == null || newDate == null) {
            throw new IllegalArgumentException("id and newDate are required");
        }

        Interview iv = em.find(Interview.class, id);
        if (iv == null)
            return null;

        iv.setScheduledAt(newDate);
        if (newMode != null && !newMode.isBlank()) {
            try {
                iv.setMode(InterviewMode.valueOf(newMode));
            } catch (IllegalArgumentException ignored) {
            }
        }
        iv.setStatus(InterviewStatus.SCHEDULED); // Reset to scheduled
        interviews.put(id, iv);
        return iv;
    }

    @Transactional
    public Interview updateInterviewDetails(UUID id, String locationOrLink) {
        if (id == null)
            return null;

        Interview iv = em.find(Interview.class, id);
        if (iv == null)
            return null;

        iv.setLocationOrLink(locationOrLink);
        interviews.put(id, iv);
        return iv;
    }

    // ======================================================
    // GETTERS (RAM cache)
    // ======================================================

    public Map<UUID, Employer> getAllEmployers() {
        return employers;
    }

    public Map<UUID, Applicant> getAllApplicants() {
        return applicants;
    }

    public Map<UUID, Company> getAllCompanies() {
        return companies;
    }

    public Map<UUID, JobOffer> getAllOffers() {
        return jobOffers;
    }

    public Map<UUID, Application> getAllApplications() {
        return applications;
    }

    public Employer getEmployer(UUID id) {
        return employers.get(id);
    }

    public Applicant getApplicant(UUID id) {
        return applicants.get(id);
    }

    public Company getCompany(UUID id) {
        return companies.get(id);
    }

    public JobOffer getOffer(UUID id) {
        return jobOffers.get(id);
    }

    public Application getApplication(UUID id) {
        return applications.get(id);
    }

    // ======================================================
    // EMPLOYERS (DB + cache)
    // ======================================================

    @Transactional
    public Employer addEmployer(Employer e) {
        if (e == null)
            throw new IllegalArgumentException("Employer cannot be null");

        if (e.getId() == null)
            e.setId(UUID.randomUUID());
        em.persist(e);

        employers.put(e.getId(), e);

        // link to company in RAM (optional)
        if (e.getCompanyId() != null) {
            Company c = companies.get(e.getCompanyId());
            if (c != null)
                c.addEmployerId(e.getId());
        }

        return e;
    }

    @Transactional
    public boolean setEmployer(UUID id, Employer updated) {
        Employer existing = em.find(Employer.class, id);
        if (existing == null)
            return false;

        // Update managed entity field-by-field to avoid wiping persisted values
        // when the client submits a partial/damaged payload.
        if (updated.getUsername() != null && !updated.getUsername().isBlank())
            existing.setUsername(updated.getUsername());
        if (updated.getPassword() != null && !updated.getPassword().isBlank())
            existing.setPassword(updated.getPassword());
        if (updated.getFirstName() != null && !updated.getFirstName().isBlank())
            existing.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null && !updated.getLastName().isBlank())
            existing.setLastName(updated.getLastName());
        if (updated.getEmail() != null && !updated.getEmail().isBlank())
            existing.setEmail(updated.getEmail());
        if (updated.getPhotoUrl() != null && !updated.getPhotoUrl().isBlank())
            existing.setPhotoUrl(updated.getPhotoUrl());

        if (updated.getDescriptionInfo() != null)
            existing.setDescriptionInfo(updated.getDescriptionInfo());
        if (updated.getEnterpriseName() != null && !updated.getEnterpriseName().isBlank())
            existing.setEnterpriseName(updated.getEnterpriseName());

        // Preserve relationship if client didn't send it
        if (updated.getCompanyId() != null) {
            existing.setCompanyId(updated.getCompanyId());
        }

        employers.put(id, existing);
        return true;
    }

    @Transactional
    public boolean removeEmployer(UUID id) {
        Employer existing = em.find(Employer.class, id);
        if (existing == null)
            return false;

        // Optional: what to do with job offers of this employer?
        // For safety, we can delete them (and their applications) too.
        List<UUID> offersToDelete = jobOffers.values().stream()
                .filter(o -> id.equals(o.getEmployerId()))
                .map(JobOffer::getId)
                .toList();

        for (UUID offerId : offersToDelete) {
            deleteJobOffer(offerId); // deletes dependent applications too
        }

        Employer managed = em.merge(existing);
        em.remove(managed);

        // RAM cleanup
        employers.remove(id);

        // detach company link in RAM
        UUID companyId = existing.getCompanyId();
        if (companyId != null) {
            Company c = companies.get(companyId);
            if (c != null) {
                c.removeEmployerId(id);
                if (id.equals(c.getOwnerEmployerId()))
                    c.setOwnerEmployerId(null);
            }
        }

        return true;
    }

    // ======================================================
    // APPLICANTS (DB + cache)
    // ======================================================

    @Transactional
    public Applicant addApplicant(Applicant a) {
        if (a == null)
            throw new IllegalArgumentException("Applicant cannot be null");

        if (a.getId() == null)
            a.setId(UUID.randomUUID());
        em.persist(a);

        applicants.put(a.getId(), a);
        return a;
    }

    @Transactional
    public boolean setApplicant(UUID id, Applicant updated) {
        Applicant existing = em.find(Applicant.class, id);
        if (existing == null)
            return false;

        updated.setId(id);

        Applicant merged = em.merge(updated);
        applicants.put(id, merged);
        return true;
    }

    @Transactional
    public boolean removeApplicant(UUID id) {
        Applicant existing = em.find(Applicant.class, id);
        if (existing == null)
            return false;

        // Delete applications of this applicant (DB + RAM)
        List<UUID> appIds = applications.values().stream()
                .filter(app -> id.equals(app.getApplicantId()))
                .map(Application::getId)
                .toList();

        for (UUID appId : appIds) {
            removeApplication(appId);
        }

        Applicant managed = em.merge(existing);
        em.remove(managed);

        applicants.remove(id);
        return true;
    }

    // ======================================================
    // COMPANIES (DB + cache)
    // ======================================================

    @Transactional
    public Company addCompany(Company c) {
        if (c == null)
            throw new IllegalArgumentException("Company cannot be null");

        if (c.getId() == null)
            c.setId(UUID.randomUUID());
        em.persist(c);

        companies.put(c.getId(), c);

        // link owner employer in RAM (optional)
        if (c.getOwnerEmployerId() != null) {
            Employer owner = employers.get(c.getOwnerEmployerId());
            if (owner != null)
                owner.setCompanyId(c.getId());
            c.addEmployerId(c.getOwnerEmployerId());
        }

        return c;
    }

    @Transactional
    public boolean setCompany(UUID id, Company updated) {
        Company existing = em.find(Company.class, id);
        if (existing == null)
            return false;

        updated.setId(id);

        // preserve owner if not sent
        if (updated.getOwnerEmployerId() == null)
            updated.setOwnerEmployerId(existing.getOwnerEmployerId());

        Company merged = em.merge(updated);
        companies.put(id, merged);
        return true;
    }

    @Transactional
    public boolean removeCompany(UUID id) {
        Company existing = em.find(Company.class, id);
        if (existing == null)
            return false;

        // Detach company from job offers (or delete offers – here we detach)
        for (JobOffer o : jobOffers.values()) {
            if (id.equals(o.getCompanyId())) {
                o.setCompanyId(null);
                JobOffer managedOffer = em.find(JobOffer.class, o.getId());
                if (managedOffer != null)
                    managedOffer.setCompanyId(null);
            }
        }

        // Detach company from employers (RAM + DB)
        for (Employer e : employers.values()) {
            if (id.equals(e.getCompanyId())) {
                e.setCompanyId(null);
                Employer managedEmp = em.find(Employer.class, e.getId());
                if (managedEmp != null)
                    managedEmp.setCompanyId(null);
            }
        }

        Company managed = em.merge(existing);
        em.remove(managed);

        companies.remove(id);
        return true;
    }

    // ======================================================
    // JOB OFFERS (DB + cache) -- IMPORTANT FIXES HERE
    // ======================================================

    /**
     * List from cache (StudyBuddy style). Sort by createdAt if available.
     */
    public List<JobOffer> listJobOffers(UUID employerId) {
        List<JobOffer> list = new ArrayList<>(jobOffers.values());
        if (employerId != null) {
            list = list.stream()
                    .filter(o -> employerId.equals(o.getEmployerId()))
                    .toList();
        }

        // sort newest first if createdAt exists; otherwise stable
        list = list.stream()
                .sorted((a, b) -> {
                    try {
                        var ta = a.getCreatedAt();
                        var tb = b.getCreatedAt();
                        if (ta == null && tb == null)
                            return 0;
                        if (ta == null)
                            return 1;
                        if (tb == null)
                            return -1;
                        return tb.compareTo(ta);
                    } catch (Exception ignore) {
                        return 0;
                    }
                })
                .toList();

        return list;
    }

    public JobOffer findJobOffer(UUID id) {
        // prefer cache; fall back to DB if needed
        JobOffer o = jobOffers.get(id);
        if (o != null)
            return o;

        JobOffer db = em.find(JobOffer.class, id);
        if (db != null) {
            jobOffers.put(id, db);
        }
        return db;
    }

    @Transactional
    public JobOffer createJobOffer(JobOffer offer) {
        if (offer == null)
            throw new IllegalArgumentException("JobOffer cannot be null");
        if (offer.getEmployerId() == null)
            throw new IllegalArgumentException("JobOffer must have employerId");

        // ensure ID exists if your entity doesn't generate it
        if (offer.getId() == null)
            offer.setId(UUID.randomUUID());

        em.persist(offer);

        // update cache
        jobOffers.put(offer.getId(), offer);

        // link inverse in RAM
        Employer e = employers.get(offer.getEmployerId());
        if (e != null)
            e.addJobOfferId(offer.getId());

        if (offer.getCompanyId() != null) {
            Company c = companies.get(offer.getCompanyId());
            if (c != null)
                c.addJobOfferId(offer.getId());
        }

        return offer;
    }

    @Transactional
    public JobOffer updateJobOffer(UUID id, JobOffer updated) {
        if (updated == null)
            throw new IllegalArgumentException("JobOffer cannot be null");

        JobOffer existing = em.find(JobOffer.class, id);
        if (existing == null)
            return null;

        // apply allowed fields
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setEmploymentType(updated.getEmploymentType());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setCompanyId(updated.getCompanyId());

        if (updated.getStatus() != null)
            existing.setStatus(updated.getStatus());

        existing.setRequiredSkills(updated.getRequiredSkills());
        existing.setRequiredQualifications(updated.getRequiredQualifications());

        // cache refresh
        jobOffers.put(id, existing);
        return existing;
    }

    @Transactional
    public boolean deleteJobOffer(UUID id) {
        JobOffer existing = em.find(JobOffer.class, id);
        if (existing == null)
            return false;

        // delete dependent applications (DB + RAM)
        List<UUID> dependentApps = applications.values().stream()
                .filter(a -> id.equals(a.getJobOfferId()))
                .map(Application::getId)
                .toList();

        for (UUID appId : dependentApps) {
            removeApplication(appId);
        }

        JobOffer managed = em.merge(existing);
        em.remove(managed);

        // RAM cleanup
        jobOffers.remove(id);

        Employer e = employers.get(existing.getEmployerId());
        if (e != null)
            e.removeJobOfferId(id);

        if (existing.getCompanyId() != null) {
            Company c = companies.get(existing.getCompanyId());
            if (c != null)
                c.removeJobOfferId(id);
        }

        return true;
    }

    @Transactional
    public JobOffer publishJobOffer(UUID offerId, UUID employerId) {
        JobOffer o = em.find(JobOffer.class, offerId);
        if (o == null)
            throw new NoSuchElementException();
        if (!Objects.equals(o.getEmployerId(), employerId)) {
            throw new SecurityException("Employer cannot publish another employer's offer.");
        }
        o.setStatus(JobOfferStatus.Published);
        jobOffers.put(o.getId(), o);
        return o;
    }

    @Transactional
    public JobOffer closeJobOffer(UUID offerId, UUID employerId) {
        JobOffer o = em.find(JobOffer.class, offerId);
        if (o == null)
            throw new NoSuchElementException();
        if (!Objects.equals(o.getEmployerId(), employerId))
            throw new SecurityException();
        o.setStatus(JobOfferStatus.Closed);
        jobOffers.put(o.getId(), o);
        return o;
    }

    @Transactional
    public JobOffer reopenJobOffer(UUID offerId, UUID employerId) {
        JobOffer o = em.find(JobOffer.class, offerId);
        if (o == null)
            throw new NoSuchElementException();
        if (!Objects.equals(o.getEmployerId(), employerId))
            throw new SecurityException();
        o.setStatus(JobOfferStatus.Reopened);
        jobOffers.put(o.getId(), o);
        return o;
    }

    // ======================================================
    // APPLICATIONS (DB + cache) -- IMPORTANT FIXES HERE
    // ======================================================

    @Transactional
    public Application addApplication(Application a) {
        if (a == null)
            throw new IllegalArgumentException("Application cannot be null");
        if (a.getJobOfferId() == null || a.getApplicantId() == null) {
            throw new IllegalArgumentException("jobOfferId and applicantId are required");
        }

        // Validate against DB (so it works even if caches were missing something)
        JobOffer offer = em.find(JobOffer.class, a.getJobOfferId());
        if (offer == null)
            throw new IllegalArgumentException("Unknown JobOffer: " + a.getJobOfferId());

        Applicant applicant = em.find(Applicant.class, a.getApplicantId());
        if (applicant == null)
            throw new IllegalArgumentException("Unknown Applicant: " + a.getApplicantId());

        // ensure ID if not generated by entity
        if (a.getId() == null)
            a.setId(UUID.randomUUID());

        // compute match score if not provided (use fresh DB entities)
        if (a.getMatchScore() == null) {
            a.setMatchScore(computeMatchScore(applicant, offer));
        }

        em.persist(a);

        // update caches (fresh)
        applications.put(a.getId(), a);
        jobOffers.put(offer.getId(), offer);
        applicants.put(applicant.getId(), applicant);

        JobOffer cachedOffer = jobOffers.get(offer.getId());
        Applicant cachedApplicant = applicants.get(applicant.getId());

        if (cachedOffer != null)
            cachedOffer.addApplicationId(a.getId());
        if (cachedApplicant != null)
            cachedApplicant.addApplicationId(a.getId());

        return a;
    }

    @Transactional
    public Application updateApplicationMatchScore(UUID id, double score) {
        Application managed = em.find(Application.class, id);
        if (managed == null)
            throw new NotFoundException("Application not found: " + id);

        managed.setMatchScore(score);

        // cache refresh
        applications.put(id, managed);
        return managed;
    }

    @Transactional
    public Application updateApplicationStatus(UUID id, ApplicationStatus status) {
        Application managed = em.find(Application.class, id);
        if (managed == null)
            throw new NotFoundException("Application not found: " + id);

        managed.setStatus(status);
        managed.setUpdatedAt(LocalDateTime.now());

        applications.put(id, managed);
        return managed;
    }

    @Transactional
    public boolean removeApplication(UUID id) {
        Application managed = em.find(Application.class, id);
        if (managed == null)
            return false;

        // update inverse RAM relations first
        JobOffer o = jobOffers.get(managed.getJobOfferId());
        if (o != null)
            o.removeApplicationId(id);

        Applicant ap = applicants.get(managed.getApplicantId());
        if (ap != null)
            ap.removeApplicationId(id);

        Application toRemove = em.merge(managed);
        em.remove(toRemove);

        applications.remove(id);
        return true;
    }

    // ======================================================
    // Helpers for JSF / UI
    // ======================================================

    public List<Application> getApplicationsByApplicantId(UUID applicantId) {
        return applications.values().stream()
                .filter(app -> applicantId.equals(app.getApplicantId()))
                .collect(Collectors.toList());
    }

    public List<Application> getApplicationsByJobId(UUID jobId) {
        return applications.values().stream()
                .filter(app -> jobId.equals(app.getJobOfferId()))
                .collect(Collectors.toList());
    }

    public List<Application> getApplicationsByEmployerId(UUID employerId) {
        List<UUID> employerJobIds = jobOffers.values().stream()
                .filter(job -> employerId.equals(job.getEmployerId()))
                .map(JobOffer::getId)
                .toList();

        return applications.values().stream()
                .filter(app -> employerJobIds.contains(app.getJobOfferId()))
                .collect(Collectors.toList());
    }

    public String getJobTitleById(UUID jobId) {
        JobOffer offer = jobOffers.get(jobId);
        return (offer != null && offer.getTitle() != null) ? offer.getTitle() : "Unknown Job";
    }

    public String getApplicantNameById(UUID applicantId) {
        Applicant a = applicants.get(applicantId);
        if (a == null)
            return "Unknown";
        String fn = a.getFirstName() == null ? "" : a.getFirstName();
        String ln = a.getLastName() == null ? "" : a.getLastName();
        String name = (fn + " " + ln).trim();
        return name.isBlank() ? "Unknown" : name;
    }

    public String getCompanyNameByJobId(UUID jobId) {
        JobOffer offer = jobOffers.get(jobId);
        if (offer == null)
            return "Unknown";
        Company c = companies.get(offer.getCompanyId());
        return (c != null && c.getName() != null) ? c.getName() : "Unknown";
    }

    // ======================================================
    // Matching helpers
    // ======================================================

    private static Set<String> tokenize(String text) {
        if (text == null)
            return Collections.emptySet();

        String[] raw = text.toLowerCase().split("[^a-z0-9+]+");
        Set<String> tokens = new HashSet<>();
        for (String t : raw) {
            t = t.trim();
            if (t.length() >= 2)
                tokens.add(t);
        }
        return tokens;
    }

    // Common synonyms/abbreviations for intelligent matching
    private static final java.util.Map<String, java.util.Set<String>> SYNONYMS = new java.util.HashMap<>();
    static {
        // Programming languages
        addSynonyms("javascript", "js", "ecmascript", "es6", "es2015");
        addSynonyms("typescript", "ts");
        addSynonyms("python", "py", "python3");
        addSynonyms("java", "jdk", "jre", "j2ee", "jakarta");
        addSynonyms("csharp", "c#", ".net", "dotnet");
        addSynonyms("cplusplus", "c++", "cpp");
        addSynonyms("golang", "go");
        addSynonyms("ruby", "rails", "ruby on rails", "ror");
        // Frameworks
        addSynonyms("react", "reactjs", "react.js");
        addSynonyms("angular", "angularjs", "angular.js");
        addSynonyms("vue", "vuejs", "vue.js");
        addSynonyms("node", "nodejs", "node.js");
        addSynonyms("spring", "spring boot", "springboot");
        addSynonyms("django", "python django");
        addSynonyms("express", "expressjs", "express.js");
        // Databases
        addSynonyms("sql", "mysql", "postgresql", "postgres", "mssql", "oracle");
        addSynonyms("nosql", "mongodb", "mongo", "cassandra", "dynamodb", "redis");
        // Cloud/DevOps
        addSynonyms("aws", "amazon web services", "ec2", "s3", "lambda");
        addSynonyms("azure", "microsoft azure");
        addSynonyms("gcp", "google cloud", "google cloud platform");
        addSynonyms("docker", "containers", "containerization");
        addSynonyms("kubernetes", "k8s");
        addSynonyms("ci/cd", "cicd", "continuous integration", "jenkins", "github actions");
        // General
        addSynonyms("frontend", "front-end", "front end", "ui", "user interface");
        addSynonyms("backend", "back-end", "back end", "server-side");
        addSynonyms("fullstack", "full-stack", "full stack");
        addSynonyms("api", "rest", "restful", "rest api", "graphql");
        addSynonyms("agile", "scrum", "kanban");
        addSynonyms("machine learning", "ml", "ai", "artificial intelligence", "deep learning");
        addSynonyms("data science", "data analysis", "analytics", "data analyst");
    }

    private static void addSynonyms(String... terms) {
        java.util.Set<String> group = new java.util.HashSet<>(java.util.Arrays.asList(terms));
        for (String t : terms) {
            SYNONYMS.computeIfAbsent(t.toLowerCase(), k -> new java.util.HashSet<>()).addAll(group);
        }
    }

    private static boolean areSynonyms(String a, String b) {
        String la = a.toLowerCase();
        String lb = b.toLowerCase();
        java.util.Set<String> synA = SYNONYMS.get(la);
        if (synA != null && synA.contains(lb))
            return true;
        java.util.Set<String> synB = SYNONYMS.get(lb);
        return synB != null && synB.contains(la);
    }

    private static double phraseSimilarity(String a, String b) {
        if (a == null || b == null)
            return 0.0;
        String sa = a.trim().toLowerCase();
        String sb = b.trim().toLowerCase();
        if (sa.isEmpty() || sb.isEmpty())
            return 0.0;

        // 1. Exact match
        if (sa.equals(sb))
            return 1.0;

        // 2. Synonym match (high confidence)
        if (areSynonyms(sa, sb))
            return 0.95;

        // 3. Contains match
        if (sa.contains(sb) || sb.contains(sa))
            return 0.75;

        // 4. Check if any tokens are synonyms
        java.util.Set<String> ta = tokenize(sa);
        java.util.Set<String> tb = tokenize(sb);
        if (ta.isEmpty() || tb.isEmpty())
            return 0.0;

        // Check for synonym overlap
        double synonymBonus = 0.0;
        for (String tokA : ta) {
            for (String tokB : tb) {
                if (areSynonyms(tokA, tokB)) {
                    synonymBonus = Math.max(synonymBonus, 0.6);
                }
            }
        }

        // 5. Token Jaccard overlap as soft similarity
        java.util.Set<String> inter = new java.util.HashSet<>(ta);
        inter.retainAll(tb);
        java.util.Set<String> union = new java.util.HashSet<>(ta);
        union.addAll(tb);
        double jaccard = union.isEmpty() ? 0.0 : (inter.size() * 1.0) / union.size();

        // 6. Levenshtein-based similarity for close matches (typos, plurals)
        double levenshtein = 0.0;
        if (Math.abs(sa.length() - sb.length()) <= 3) {
            int dist = levenshteinDistance(sa, sb);
            int maxLen = Math.max(sa.length(), sb.length());
            if (maxLen > 0 && dist <= 3) {
                levenshtein = 1.0 - ((double) dist / maxLen);
                if (levenshtein > 0.7)
                    levenshtein *= 0.6; // Scale down but still credit
            }
        }

        return Math.max(Math.max(jaccard, synonymBonus), levenshtein);
    }

    private static int levenshteinDistance(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        int[] curr = new int[s2.length() + 1];
        for (int j = 0; j <= s2.length(); j++)
            prev[j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[s2.length()];
    }

    private static double listSimilarity(java.util.Collection<String> requirements,
            java.util.Collection<String> candidatePhrases) {
        if (requirements == null || requirements.isEmpty())
            return 0.0;
        if (candidatePhrases == null || candidatePhrases.isEmpty())
            return 0.0;

        double sum = 0.0;
        int n = 0;
        for (String r : requirements) {
            if (r == null || r.isBlank())
                continue;
            double best = 0.0;
            for (String c : candidatePhrases) {
                best = Math.max(best, phraseSimilarity(r, c));
                if (best >= 1.0)
                    break;
            }
            sum += best;
            n++;
        }
        if (n == 0)
            return 0.0;
        return (sum / n) * 100.0;
    }

    private double computeMatchScore(Applicant applicant, JobOffer offer) {
        // collect applicant phrases
        java.util.LinkedHashSet<String> applicantPhrases = new java.util.LinkedHashSet<>();
        if (applicant.getSkills() != null) {
            for (String s : applicant.getSkills()) {
                if (s != null && !s.isBlank())
                    applicantPhrases.add(s.trim().toLowerCase());
            }
        }
        if (applicantPhrases.isEmpty()) {
            String skillsStr = applicant.getSkillsAsString();
            if (skillsStr != null && !skillsStr.isBlank()) {
                for (String s : skillsStr.split(",")) {
                    String t = s.trim().toLowerCase();
                    if (!t.isBlank())
                        applicantPhrases.add(t);
                }
            }
        }
        if (applicantPhrases.isEmpty())
            return 0.0;

        java.util.List<String> reqSkills = offer.getRequiredSkills();
        java.util.List<String> reqQuals = offer.getRequiredQualifications();

        boolean hasSkills = reqSkills != null && !reqSkills.isEmpty();
        boolean hasQuals = reqQuals != null && !reqQuals.isEmpty();

        if (hasSkills || hasQuals) {
            double skillsScore = hasSkills ? listSimilarity(reqSkills, applicantPhrases) : 0.0;
            double qualsScore = hasQuals ? listSimilarity(reqQuals, applicantPhrases) : 0.0;

            double result;
            if (hasSkills && hasQuals) {
                result = 0.7 * skillsScore + 0.3 * qualsScore;
            } else {
                result = hasSkills ? skillsScore : qualsScore;
            }
            return Math.round(result * 10.0) / 10.0;
        }

        // Fallback: title/description token overlap
        StringBuilder jobText = new StringBuilder();
        if (offer.getTitle() != null)
            jobText.append(offer.getTitle()).append(" ");
        if (offer.getDescription() != null)
            jobText.append(offer.getDescription());
        java.util.Set<String> jobTokens = tokenize(jobText.toString());
        if (jobTokens.isEmpty())
            return 0.0;

        java.util.Set<String> applicantTokens = new java.util.HashSet<>();
        for (String phrase : applicantPhrases) {
            applicantTokens.addAll(tokenize(phrase));
        }
        if (applicantTokens.isEmpty())
            return 0.0;

        int matches = 0;
        for (String s : applicantTokens)
            if (jobTokens.contains(s))
                matches++;

        double raw = (matches * 100.0) / applicantTokens.size();
        return Math.round(raw * 10.0) / 10.0;
    }

    // ======================================================
    // DB admin endpoints (populate/clear/reset) - StudyBuddy style
    // ======================================================

    @Transactional
    public void clearDB() {
        clearObjects();

        // children -> parents
        try {
            em.createQuery("DELETE FROM Interview").executeUpdate();
        } catch (Exception ignored) {
        }
        em.createQuery("DELETE FROM Application").executeUpdate();
        em.createQuery("DELETE FROM JobOffer").executeUpdate();
        em.createQuery("DELETE FROM Employer").executeUpdate();
        em.createQuery("DELETE FROM Applicant").executeUpdate();
        em.createQuery("DELETE FROM Company").executeUpdate();
    }

    @Transactional
    public void populateDB() {
        clearDB();
        populateApplicationState();
        // After populate, reload caches from DB to ensure everything is consistent
        loadFromDatabase();
    }

    @Transactional
    public void resetDB() {
        populateDB();
    }

    // ======================================================
    // Your seed logic (unchanged): uses
    // addEmployer/addCompany/addApplicant/createJobOffer/addApplication
    // ======================================================

    private void populateApplicationState() {
        clearObjects();

        // ========= EMPLOYERS =========
        Employer alice = new Employer();
        alice.setFirstName("Alice");
        alice.setLastName("Martin");
        alice.setEmail("alice@example.com");
        alice.setUsername("alice");
        alice.setPassword("alice123");
        addEmployer(alice);

        Employer bob = new Employer();
        bob.setFirstName("Bob");
        bob.setLastName("Dupont");
        bob.setEmail("bob@example.com");
        bob.setUsername("bob");
        bob.setPassword("bob123");
        addEmployer(bob);

        Employer carla = new Employer();
        carla.setFirstName("Carla");
        carla.setLastName("Rossi");
        carla.setEmail("carla@example.com");
        carla.setUsername("carla");
        carla.setPassword("carla123");
        addEmployer(carla);

        Employer david = new Employer();
        david.setFirstName("David");
        david.setLastName("Nguyen");
        david.setEmail("david@example.com");
        david.setUsername("david");
        david.setPassword("david123");
        addEmployer(david);

        // ========= COMPANIES =========
        Company acme = new Company();
        acme.setName("Acme SA");
        acme.setDescription("General tech company");
        acme.setLocation("Lausanne");
        acme.setOwnerEmployerId(alice.getId());
        addCompany(acme);

        Company dataWorks = new Company();
        dataWorks.setName("DataWorks GmbH");
        dataWorks.setDescription("Data consulting and analytics");
        dataWorks.setLocation("Zurich");
        dataWorks.setOwnerEmployerId(bob.getId());
        addCompany(dataWorks);

        Company greenFuture = new Company();
        greenFuture.setName("GreenFuture AG");
        greenFuture.setDescription("Sustainability solutions");
        greenFuture.setLocation("Geneva");
        greenFuture.setOwnerEmployerId(carla.getId());
        addCompany(greenFuture);

        Company finEdge = new Company();
        finEdge.setName("FinEdge SA");
        finEdge.setDescription("Fintech products and services");
        finEdge.setLocation("Geneva");
        finEdge.setOwnerEmployerId(bob.getId());
        addCompany(finEdge);

        Company healthSync = new Company();
        healthSync.setName("HealthSync SA");
        healthSync.setDescription("Digital health & mHealth");
        healthSync.setLocation("Lausanne");
        healthSync.setOwnerEmployerId(david.getId());
        addCompany(healthSync);

        Company roboLabs = new Company();
        roboLabs.setName("RoboLabs SARL");
        roboLabs.setDescription("Robotics & automation");
        roboLabs.setLocation("Bern");
        roboLabs.setOwnerEmployerId(alice.getId());
        addCompany(roboLabs);

        Company cloudNova = new Company();
        cloudNova.setName("CloudNova SA");
        cloudNova.setDescription("Cloud infrastructure & DevOps consulting");
        cloudNova.setLocation("Basel");
        cloudNova.setOwnerEmployerId(david.getId());
        addCompany(cloudNova);

        Company eduTech = new Company();
        eduTech.setName("EduTech Labs");
        eduTech.setDescription("Digital learning platforms and tools");
        eduTech.setLocation("Fribourg");
        eduTech.setOwnerEmployerId(alice.getId());
        addCompany(eduTech);

        Company alpineLogistics = new Company();
        alpineLogistics.setName("Alpine Logistics SA");
        alpineLogistics.setDescription("Logistics & supply-chain optimization");
        alpineLogistics.setLocation("Lugano");
        alpineLogistics.setOwnerEmployerId(bob.getId());
        addCompany(alpineLogistics);

        // ========= APPLICANTS =========
        Applicant igor = new Applicant();
        igor.setFirstName("Igor");
        igor.setLastName("Zolotarev");
        igor.setEmail("igor.applicant@example.com");
        igor.setUsername("igor");
        igor.setPassword("igor123");
        igor.setSkills(java.util.Arrays.asList("java", "python", "sql"));
        addApplicant(igor);

        Applicant sara = new Applicant();
        sara.setFirstName("Sara");
        sara.setLastName("Novak");
        sara.setEmail("sara@example.com");
        sara.setUsername("sara");
        sara.setPassword("sara123");
        sara.setSkills(java.util.Arrays.asList("javascript", "css", "react"));
        addApplicant(sara);

        Applicant tom = new Applicant();
        tom.setFirstName("Tom");
        tom.setLastName("Candidate");
        tom.setEmail("tom@example.com");
        tom.setUsername("tom");
        tom.setPassword("tom123");
        tom.setSkills(java.util.Arrays.asList("docker", "kubernetes", "linux"));
        addApplicant(tom);

        Applicant emma = new Applicant();
        emma.setFirstName("Emma");
        emma.setLastName("Liu");
        emma.setEmail("emma@example.com");
        emma.setUsername("emma");
        emma.setPassword("emma123");
        emma.setSkills(java.util.Arrays.asList("python", "statistics", "sql"));
        addApplicant(emma);

        Applicant marc = new Applicant();
        marc.setFirstName("Marc");
        marc.setLastName("Dubois");
        marc.setEmail("marc@example.com");
        marc.setUsername("marc");
        marc.setPassword("marc123");
        marc.setSkills(java.util.Arrays.asList("javascript", "ux", "figma"));
        addApplicant(marc);

        Applicant julia = new Applicant();
        julia.setFirstName("Julia");
        julia.setLastName("Rossi");
        julia.setEmail("julia@example.com");
        julia.setUsername("julia");
        julia.setPassword("julia123");
        julia.setSkills(java.util.Arrays.asList("aws", "terraform", "cloud"));
        addApplicant(julia);

        Applicant nico = new Applicant();
        nico.setFirstName("Nicolas");
        nico.setLastName("Keller");
        nico.setEmail("nicolas@example.com");
        nico.setUsername("nicolas");
        nico.setPassword("nico123");
        nico.setSkills(java.util.Arrays.asList("robotics", "c++", "python"));
        addApplicant(nico);

        Applicant lina = new Applicant();
        lina.setFirstName("Lina");
        lina.setLastName("Müller");
        lina.setEmail("lina@example.com");
        lina.setUsername("lina");
        lina.setPassword("lina123");
        lina.setSkills(java.util.Arrays.asList("operations", "management", "supply chain"));
        addApplicant(lina);

        Applicant omar = new Applicant();
        omar.setFirstName("Omar");
        omar.setLastName("Haddad");
        omar.setEmail("omar@example.com");
        omar.setUsername("omar");
        omar.setPassword("omar123");
        omar.setSkills(java.util.Arrays.asList("product management", "agile", "fintech"));
        addApplicant(omar);

        Applicant chloe = new Applicant();
        chloe.setFirstName("Chloé");
        chloe.setLastName("Morel");
        chloe.setEmail("chloe@example.com");
        chloe.setUsername("chloe");
        chloe.setPassword("chloe123");
        chloe.setSkills(java.util.Arrays.asList("ux", "ui", "figma"));
        addApplicant(chloe);

        // ========= JOB OFFERS =========
        // Use your JobOfferStatus enum: adjust names if different (e.g.
        // PUBLISHED/DRAFT/CLOSED)
        JobOffer o1 = new JobOffer();
        o1.setTitle("Junior Java Developer");
        o1.setDescription("Work on backend services in Java for our web platform.");
        o1.setEmployerId(alice.getId());
        o1.setCompanyId(acme.getId());
        o1.setStatus(JobOfferStatus.Published);
        o1.setRequiredSkills(java.util.Arrays.asList("java", "sql", "spring"));
        createJobOffer(o1);

        JobOffer o2 = new JobOffer();
        o2.setTitle("Data Analyst Intern");
        o2.setDescription("Help analyse job market and platform usage data.");
        o2.setEmployerId(bob.getId());
        o2.setCompanyId(dataWorks.getId());
        o2.setStatus(JobOfferStatus.Published);
        o2.setRequiredSkills(java.util.Arrays.asList("python", "sql", "statistics", "data warehouse"));
        createJobOffer(o2);

        JobOffer o3 = new JobOffer();
        o3.setTitle("DevOps Engineer");
        o3.setDescription("Maintain CI/CD pipelines and cloud infrastructure.");
        o3.setEmployerId(carla.getId());
        o3.setCompanyId(greenFuture.getId());
        o3.setStatus(JobOfferStatus.Draft);
        o3.setRequiredSkills(java.util.Arrays.asList("docker", "kubernetes", "ci/cd", "linux"));
        createJobOffer(o3);

        JobOffer o4 = new JobOffer();
        o4.setTitle("Frontend Developer");
        o4.setDescription("Build and style the JobFinder web UI.");
        o4.setEmployerId(alice.getId());
        o4.setCompanyId(acme.getId());
        o4.setStatus(JobOfferStatus.Published);
        o4.setRequiredSkills(java.util.Arrays.asList("javascript", "css", "react"));
        createJobOffer(o4);

        JobOffer o5 = new JobOffer();
        o5.setTitle("Machine Learning Engineer");
        o5.setDescription("Develop matching and recommendation models.");
        o5.setEmployerId(bob.getId());
        o5.setCompanyId(dataWorks.getId());
        o5.setStatus(JobOfferStatus.Closed);
        o5.setRequiredSkills(java.util.Arrays.asList("python", "tensorflow", "ml", "data"));
        createJobOffer(o5);

        JobOffer o6 = new JobOffer();
        o6.setTitle("Sustainability Consultant");
        o6.setDescription("Advise clients on green transformation projects.");
        o6.setEmployerId(carla.getId());
        o6.setCompanyId(greenFuture.getId());
        o6.setStatus(JobOfferStatus.Published);
        o6.setRequiredSkills(java.util.Arrays.asList("sustainability", "consulting"));
        createJobOffer(o6);

        JobOffer o7 = new JobOffer();
        o7.setTitle("Product Manager – Fintech");
        o7.setDescription("Own product roadmap for a digital payment solution.");
        o7.setEmployerId(bob.getId());
        o7.setCompanyId(finEdge.getId());
        o7.setStatus(JobOfferStatus.Published);
        o7.setRequiredSkills(java.util.Arrays.asList("product management", "fintech", "agile"));
        createJobOffer(o7);

        JobOffer o8 = new JobOffer();
        o8.setTitle("Mobile Developer (iOS/Android)");
        o8.setDescription("Develop our health monitoring mobile app.");
        o8.setEmployerId(david.getId());
        o8.setCompanyId(healthSync.getId());
        o8.setStatus(JobOfferStatus.Draft);
        o8.setRequiredSkills(java.util.Arrays.asList("android", "ios", "mobile"));
        createJobOffer(o8);

        JobOffer o9 = new JobOffer();
        o9.setTitle("Robotics Engineer Intern");
        o9.setDescription("Support development of warehouse robotics projects.");
        o9.setEmployerId(alice.getId());
        o9.setCompanyId(roboLabs.getId());
        o9.setStatus(JobOfferStatus.Published);
        o9.setRequiredSkills(java.util.Arrays.asList("robotics", "python", "c++"));
        createJobOffer(o9);

        JobOffer o10 = new JobOffer();
        o10.setTitle("Cloud Architect");
        o10.setDescription("Design scalable cloud architectures for enterprise clients.");
        o10.setEmployerId(david.getId());
        o10.setCompanyId(cloudNova.getId());
        o10.setStatus(JobOfferStatus.Published);
        o10.setRequiredSkills(java.util.Arrays.asList("aws", "azure", "cloud", "terraform"));
        createJobOffer(o10);

        JobOffer o11 = new JobOffer();
        o11.setTitle("UX/UI Designer");
        o11.setDescription("Design intuitive interfaces for learning platforms.");
        o11.setEmployerId(alice.getId());
        o11.setCompanyId(eduTech.getId());
        o11.setStatus(JobOfferStatus.Draft);
        o11.setRequiredSkills(java.util.Arrays.asList("ux", "ui", "figma"));
        createJobOffer(o11);

        JobOffer o12 = new JobOffer();
        o12.setTitle("Operations Manager");
        o12.setDescription("Oversee logistics operations and process improvement.");
        o12.setEmployerId(bob.getId());
        o12.setCompanyId(alpineLogistics.getId());
        o12.setStatus(JobOfferStatus.Published);
        o12.setRequiredSkills(java.util.Arrays.asList("operations", "supply chain", "management"));
        createJobOffer(o12);

        // ========= APPLICATIONS =========
        // Adjust enum names if needed (SUBMITTED, IN_REVIEW, REJECTED, HIRED, ...)

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // Igor applies to Junior Java Dev (Alice / Acme)
        Application a1App = new Application();
        a1App.setJobOfferId(o1.getId());
        a1App.setApplicantId(igor.getId());
        a1App.setStatus(ApplicationStatus.Submitted);
        a1App.setMatchScore(82.5);
        a1App.setSubmittedAt(now.minusDays(5));
        addApplication(a1App);

        // Sara applies to Junior Java Dev – in review
        Application a2App = new Application();
        a2App.setJobOfferId(o1.getId());
        a2App.setApplicantId(sara.getId());
        a2App.setStatus(ApplicationStatus.In_review);
        a2App.setMatchScore(74.0);
        a2App.setSubmittedAt(now.minusDays(3));
        addApplication(a2App);

        // Marc applies to Frontend Dev (Alice / Acme)
        Application a3App = new Application();
        a3App.setJobOfferId(o4.getId());
        a3App.setApplicantId(marc.getId());
        a3App.setStatus(ApplicationStatus.Submitted);
        a3App.setMatchScore(77.5);
        a3App.setSubmittedAt(now.minusDays(1));
        addApplication(a3App);

        // Emma applies to Data Analyst Intern (Bob / DataWorks)
        Application a4App = new Application();
        a4App.setJobOfferId(o2.getId());
        a4App.setApplicantId(emma.getId());
        a4App.setStatus(ApplicationStatus.Submitted);
        a4App.setMatchScore(88.0);
        a4App.setSubmittedAt(now.minusDays(4));
        addApplication(a4App);

        // Tom applies to Machine Learning Engineer (closed offer)
        Application a5App = new Application();
        a5App.setJobOfferId(o5.getId());
        a5App.setApplicantId(tom.getId());
        a5App.setStatus(ApplicationStatus.Rejected);
        a5App.setMatchScore(69.0);
        a5App.setSubmittedAt(now.minusDays(10));
        addApplication(a5App);

        // Lina applies to Sustainability Consultant (Carla / GreenFuture)
        Application a6App = new Application();
        a6App.setJobOfferId(o6.getId());
        a6App.setApplicantId(lina.getId());
        a6App.setStatus(ApplicationStatus.Submitted);
        a6App.setMatchScore(91.0);
        a6App.setSubmittedAt(now.minusDays(2));
        addApplication(a6App);

        // Omar applies to Product Manager – Fintech
        Application a7App = new Application();
        a7App.setJobOfferId(o7.getId());
        a7App.setApplicantId(omar.getId());
        a7App.setStatus(ApplicationStatus.In_review);
        a7App.setMatchScore(79.5);
        a7App.setSubmittedAt(now.minusDays(7));
        addApplication(a7App);

        // Chloé applies to Mobile Dev (draft offer – demo edge case)
        Application a8App = new Application();
        a8App.setJobOfferId(o8.getId());
        a8App.setApplicantId(chloe.getId());
        a8App.setStatus(ApplicationStatus.Submitted);
        a8App.setMatchScore(86.0);
        a8App.setSubmittedAt(now.minusDays(1));
        addApplication(a8App);

        // Nico applies to Robotics Engineer Intern
        Application a9App = new Application();
        a9App.setJobOfferId(o9.getId());
        a9App.setApplicantId(nico.getId());
        a9App.setStatus(ApplicationStatus.Submitted);
        a9App.setMatchScore(80.0);
        a9App.setSubmittedAt(now.minusDays(6));
        addApplication(a9App);

        // Emma also applies to Cloud Architect
        Application a10App = new Application();
        a10App.setJobOfferId(o10.getId());
        a10App.setApplicantId(emma.getId());
        a10App.setStatus(ApplicationStatus.Submitted);
        a10App.setMatchScore(84.0);
        a10App.setSubmittedAt(now.minusDays(3));
        addApplication(a10App);

        // Igor applies to Cloud Architect as well
        Application a11App = new Application();
        a11App.setJobOfferId(o10.getId());
        a11App.setApplicantId(igor.getId());
        a11App.setStatus(ApplicationStatus.In_review);
        a11App.setMatchScore(90.0);
        a11App.setSubmittedAt(now.minusDays(2));
        addApplication(a11App);

        // Chloé applies to UX/UI Designer (draft)
        Application a12App = new Application();
        a12App.setJobOfferId(o11.getId());
        a12App.setApplicantId(chloe.getId());
        a12App.setStatus(ApplicationStatus.Submitted);
        a12App.setMatchScore(89.0);
        a12App.setSubmittedAt(now.minusDays(1));
        addApplication(a12App);

        // Omar applies to Operations Manager
        Application a13App = new Application();
        a13App.setJobOfferId(o12.getId());
        a13App.setApplicantId(omar.getId());
        a13App.setStatus(ApplicationStatus.Submitted);
        a13App.setMatchScore(76.0);
        a13App.setSubmittedAt(now.minusDays(8));
        addApplication(a13App);

        // Lina applies to Operations Manager
        Application a14App = new Application();
        a14App.setJobOfferId(o12.getId());
        a14App.setApplicantId(lina.getId());
        a14App.setStatus(ApplicationStatus.In_review);
        a14App.setMatchScore(82.0);
        a14App.setSubmittedAt(now.minusDays(4));
        addApplication(a14App);

        // Recompute all seeded applications to override any hardcoded scores
        for (Applicant a : applicants.values()) {
            try {
                recomputeMatchScoresForApplicant(a.getId());
            } catch (Exception ignore) {
            }
        }
    }

    // ======================================================
    // APPLICATIONS API (names used by your Resource)
    // ======================================================

    public List<Application> listApplications() {
        return new ArrayList<>(applications.values());
    }

    public Application findApplication(UUID id) {
        // prefer cache, fallback to DB
        Application a = applications.get(id);
        if (a != null)
            return a;

        Application db = em.find(Application.class, id);
        if (db != null)
            applications.put(id, db);
        return db;
    }

    public List<Application> listApplicationsByOfferId(UUID jobOfferId) {
        List<Application> list = em.createQuery(
                "SELECT a FROM Application a WHERE a.jobOfferId = :jobOfferId", Application.class)
                .setParameter("jobOfferId", jobOfferId)
                .getResultList();

        for (Application a : list) {
            if (a != null && a.getId() != null) {
                applications.put(a.getId(), a);
            }
        }
        return list;
    }

    public List<Application> listApplicationsByApplicantId(UUID applicantId) {
        List<Application> list = em.createQuery(
                "SELECT a FROM Application a WHERE a.applicantId = :applicantId", Application.class)
                .setParameter("applicantId", applicantId)
                .getResultList();

        for (Application a : list) {
            if (a != null && a.getId() != null) {
                applications.put(a.getId(), a);
            }
        }
        return list;
    }

    @Transactional
    public Application createApplication(Application a) {
        return addApplication(a); // your existing DB+cache method
    }

    @Transactional
    public Application updateApplication(UUID id, Application updated) {
        Application existing = em.find(Application.class, id);
        if (existing == null)
            return null;

        // Keep identity stable
        updated.setId(id);

        // Usually jobOfferId/applicantId should NOT change for an application:
        // keep original IDs even if client sends something else
        updated.setJobOfferId(existing.getJobOfferId());
        updated.setApplicantId(existing.getApplicantId());

        // Copy fields you allow to change (safe defaults)
        existing.setStatus(updated.getStatus());
        existing.setMatchScore(updated.getMatchScore());
        existing.setSubmittedAt(updated.getSubmittedAt()); // if you allow it
        existing.setUpdatedAt(LocalDateTime.now());

        applications.put(id, existing);
        return existing;
    }

    @Transactional
    public boolean deleteApplication(UUID id) {
        return removeApplication(id); // your DB+cache remove method
    }

    @Transactional
    public int recomputeMatchScoresForApplicant(UUID applicantId) {
        Applicant applicant = em.find(Applicant.class, applicantId);
        if (applicant == null)
            throw new NotFoundException("Applicant not found");

        // refresh cache with the latest managed entity
        applicants.put(applicantId, applicant);

        List<Application> apps = listApplicationsByApplicantId(applicantId);

        int updated = 0;
        for (Application app : apps) {
            JobOffer offer = em.find(JobOffer.class, app.getJobOfferId());
            if (offer == null)
                continue;

            // also keep offer fresh in cache
            jobOffers.put(offer.getId(), offer);

            double score = computeMatchScore(applicant, offer);

            updateApplicationMatchScore(app.getId(), score);
            updated++;
        }

        return updated;
    }

    // ======================================================
    // APPLICANTS API (names used by your Resource)
    // ======================================================

    public List<Applicant> listApplicants() {
        return new ArrayList<>(applicants.values());
    }

    public Applicant findApplicant(UUID id) {
        Applicant cached = applicants.get(id);
        if (cached != null)
            return cached;

        List<Applicant> list = em.createQuery(
                "SELECT a FROM Applicant a LEFT JOIN FETCH a.skills WHERE a.id = :id", Applicant.class)
                .setParameter("id", id).getResultList();

        Applicant db = list.isEmpty() ? null : list.get(0);
        if (db != null)
            applicants.put(id, db);
        return db;
    }

    @Transactional
    public Applicant createApplicant(Applicant a) {
        return addApplicant(a);
    }

    @Transactional
    public Applicant updateApplicant(UUID id, Applicant updated) {
        Applicant managed = em.find(Applicant.class, id);
        if (managed == null)
            return null;

        // Copy scalar fields
        managed.setFirstName(updated.getFirstName());
        managed.setLastName(updated.getLastName());
        managed.setEmail(updated.getEmail());
        managed.setPhotoUrl(updated.getPhotoUrl());
        managed.setContactInfo(updated.getContactInfo());
        managed.setDescriptionInfo(updated.getDescriptionInfo());
        managed.setCvInfo(updated.getCvInfo());

        // Critically handle @ElementCollection updates
        managed.getSkills().clear();
        if (updated.getSkills() != null) {
            managed.getSkills().addAll(updated.getSkills());
        }

        // Refresh cache
        applicants.put(id, managed);
        return managed;
    }

    @Transactional
    public boolean deleteApplicant(UUID id) {
        return removeApplicant(id);
    }

    @Transactional
    public Applicant updateApplicantProfile(UUID id, Applicant incoming) {
        Applicant managed = em.find(Applicant.class, id);
        if (managed == null)
            throw new NotFoundException("Applicant not found");

        if (incoming.getFirstName() != null && !incoming.getFirstName().isBlank())
            managed.setFirstName(incoming.getFirstName());
        if (incoming.getLastName() != null && !incoming.getLastName().isBlank())
            managed.setLastName(incoming.getLastName());
        if (incoming.getEmail() != null && !incoming.getEmail().isBlank())
            managed.setEmail(incoming.getEmail());
        
        // Update password if provided
        if (incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
            managed.setPassword(incoming.getPassword());
        }

        if (incoming.getPhotoUrl() != null && !incoming.getPhotoUrl().isBlank())
            managed.setPhotoUrl(incoming.getPhotoUrl());
        if (incoming.getContactInfo() != null)
            managed.setContactInfo(incoming.getContactInfo());
        if (incoming.getDescriptionInfo() != null)
            managed.setDescriptionInfo(incoming.getDescriptionInfo());
        if (incoming.getCvInfo() != null)
            managed.setCvInfo(incoming.getCvInfo());

        // Robust ElementCollection update
        if (incoming.getSkills() != null) {
            managed.getSkills().clear();
            managed.getSkills().addAll(incoming.getSkills());
        }

        applicants.put(id, managed); // refresh cache
        return managed;
    }

    // ======================================================
    // COMPANIES API (names used by your Resource)
    // ======================================================

    public List<Company> listCompanies() {
        return new ArrayList<>(companies.values());
    }

    public Company findCompany(UUID id) {
        Company c = companies.get(id);
        if (c != null)
            return c;

        Company db = em.find(Company.class, id);
        if (db != null)
            companies.put(id, db);
        return db;
    }

    @Transactional
    public Company createCompany(Company c) {
        return addCompany(c);
    }

    @Transactional
    public Company updateCompany(UUID id, Company updated) {
        boolean ok = setCompany(id, updated);
        return ok ? findCompany(id) : null;
    }

    @Transactional
    public boolean deleteCompany(UUID id) {
        return removeCompany(id);
    }

    // ======================================================
    // EMPLOYERS API (names used by your Resource)
    // ======================================================

    public List<Employer> listEmployers() {
        return new ArrayList<>(employers.values());
    }

    public Employer findEmployer(UUID id) {
        Employer e = employers.get(id);
        if (e != null)
            return e;

        Employer db = em.find(Employer.class, id);
        if (db != null)
            employers.put(id, db);
        return db;
    }

    @Transactional
    public Employer createEmployer(Employer e) {
        return addEmployer(e);
    }

    @Transactional
    public Employer updateEmployer(UUID id, Employer updated) {
        boolean ok = setEmployer(id, updated);
        return ok ? findEmployer(id) : null;
    }

    @Transactional
    public boolean deleteEmployer(UUID id) {
        return removeEmployer(id);
    }

    public List<Company> listCompaniesByOwnerEmployerId(UUID ownerEmployerId) {
        return companies.values().stream()
                .filter(c -> ownerEmployerId.equals(c.getOwnerEmployerId()))
                .collect(Collectors.toList());
    }

}
