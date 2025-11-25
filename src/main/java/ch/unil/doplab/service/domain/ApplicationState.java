package ch.unil.doplab.service.domain;

import ch.unil.doplab.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;


/**
 * ApplicationState est le "stockage mémoire" central de JobFinder.
 * Il gère toutes les entités : Employers, Applicants, Companies,
 * JobOffers, Applications.
 */
@ApplicationScoped
public class ApplicationState {

    // ======================================================
    // STOCKAGE DES ENTITÉS
    // ======================================================

    private final Map<UUID, Employer> employers = new HashMap<>();
    private final Map<UUID, Applicant> applicants = new HashMap<>();
    private final Map<UUID, Company> companies = new HashMap<>();
    private final Map<UUID, JobOffer> jobOffers = new HashMap<>();
    private final Map<UUID, Application> applications = new HashMap<>();

    @PostConstruct
    private void seedDemoData() {
        // ===== EMPLOYERS =====
        Employer emp1 = new Employer();
        emp1.setName("Alice", "Martin");              // adjust to your field names
        emp1.setEmail("alice@example.com");       // e.g. setEmail if exists
        addEmployer(emp1);

        Employer emp2 = new Employer();
        emp2.setName("Bob", "Dupont");
        emp2.setEmail("bob@example.com");
        addEmployer(emp2);

        Employer emp3 = new Employer();
        emp3.setName("Carla", "Rossi");
        emp3.setEmail("carla@example.com");
        addEmployer(emp3);

        // ===== COMPANIES (owned by employers) =====
        Company c1 = new Company();
        c1.setName("Acme SA");                    // adjust fields
        c1.setDescription("General tech company");
        c1.setOwnerEmployerId(emp1.getId());      // addCompany() will also link employer->company
        addCompany(c1);

        Company c2 = new Company();
        c2.setName("DataWorks GmbH");
        c2.setDescription("Data consulting");
        c2.setOwnerEmployerId(emp2.getId());
        addCompany(c2);

        Company c3 = new Company();
        c3.setName("GreenFuture AG");
        c3.setDescription("Sustainability solutions");
        c3.setOwnerEmployerId(emp3.getId());
        addCompany(c3);

        // ===== APPLICANTS =====
        Applicant a1 = new Applicant();
        a1.setName("Igor", "Test");                  // adjust field names (e.g. setFirstName/setLastName)
        a1.setEmail("igor.applicant1@example.com");
        addApplicant(a1);

        Applicant a2 = new Applicant();
        a2.setName("Sara", "Applicant");
        a2.setEmail("sara@example.com");
        addApplicant(a2);

        Applicant a3 = new Applicant();
        a3.setName("Tom", "Candidate");
        a3.setEmail("tom@example.com");
        addApplicant(a3);

        // ===== JOB OFFERS =====
        JobOffer o1 = new JobOffer();
        o1.setTitle("Junior Java Developer");     // adjust to your fields
        o1.setDescription("Work on backend services in Java.");
        o1.setEmployerId(emp1.getId());
        o1.setCompanyId(c1.getId());
        addOffer(o1);                             // will link to employer and company

        JobOffer o2 = new JobOffer();
        o2.setTitle("Data Analyst Intern");
        o2.setDescription("Help analyse job market data.");
        o2.setEmployerId(emp2.getId());
        o2.setCompanyId(c2.getId());
        addOffer(o2);

        JobOffer o3 = new JobOffer();
        o3.setTitle("DevOps Engineer");
        o3.setDescription("Maintain CI/CD and deployment pipelines.");
        o3.setEmployerId(emp3.getId());
        o3.setCompanyId(c3.getId());
        addOffer(o3);
    }



    // ======================================================
    // GETTERS
    // ======================================================

    public Map<UUID, Employer> getAllEmployers() { return employers; }
    public Map<UUID, Applicant> getAllApplicants() { return applicants; }
    public Map<UUID, Company> getAllCompanies() { return companies; }
    public Map<UUID, JobOffer> getAllOffers() { return jobOffers; }
    public Map<UUID, Application> getAllApplications() { return applications; }

    public Employer getEmployer(UUID id) { return employers.get(id); }
    public Applicant getApplicant(UUID id) { return applicants.get(id); }
    public Company getCompany(UUID id) { return companies.get(id); }
    public JobOffer getOffer(UUID id) { return jobOffers.get(id); }
    public Application getApplication(UUID id) { return applications.get(id); }


    // ======================================================
    // EMPLOYERS
    // ======================================================

    public Employer addEmployer(Employer e) {
        if (e == null) throw new IllegalArgumentException("Employer cannot be null.");

        UUID id = e.getId() != null ? e.getId() : UUID.randomUUID();
        e.setId(id);

        employers.put(id, e);

        // Lier à sa Company si c'est spécifié
        if (e.getCompanyId() != null) {
            Company c = companies.get(e.getCompanyId());
            if (c != null) c.addEmployerId(id);
        }

        return e;
    }


    // ======================================================
    // APPLICANTS
    // ======================================================

    public Applicant addApplicant(Applicant a) {
        if (a == null) throw new IllegalArgumentException("Applicant cannot be null.");

        UUID id = a.getId() != null ? a.getId() : UUID.randomUUID();
        a.setId(id);

        applicants.put(id, a);
        return a;
    }


    // ======================================================
    // COMPANIES
    // ======================================================

    public Company addCompany(Company c) {
        if (c == null) throw new IllegalArgumentException("Company cannot be null.");

        UUID id = c.getId() != null ? c.getId() : UUID.randomUUID();
        c.setId(id);

        companies.put(id, c);

        // Ajouter l'owner employer dans la liste
        if (c.getOwnerEmployerId() != null) {
            Employer owner = employers.get(c.getOwnerEmployerId());
            if (owner != null) owner.setCompanyId(id);
            c.addEmployerId(c.getOwnerEmployerId());
        }

        return c;
    }


    // ======================================================
    // JOB OFFERS
    // ======================================================

    public JobOffer addOffer(JobOffer o) {
        if (o == null) throw new IllegalArgumentException("JobOffer cannot be null.");
        if (o.getEmployerId() == null) throw new IllegalArgumentException("JobOffer must have employerId");

        UUID id = o.getId() != null ? o.getId() : UUID.randomUUID();
        o.setId(id);

        // Valeurs par défaut
        if (o.getStatus() == null) o.setStatus(JobOfferStatus.Draft);
        if (o.getCreatedAt() == null) o.setCreatedAt(LocalDateTime.now());

        jobOffers.put(id, o);

        // Lier à l'employer
        Employer emp = employers.get(o.getEmployerId());
        if (emp != null) emp.addJobOfferId(id);

        // Lier à la company
        if (o.getCompanyId() != null) {
            Company c = companies.get(o.getCompanyId());
            if (c != null) c.addJobOfferId(id);
        }

        return o;
    }

    public boolean setOffer(UUID id, JobOffer updated) {
        JobOffer existing = jobOffers.get(id);
        if (existing == null) return false;

        updated.setId(id);
        jobOffers.put(id, updated);
        return true;
    }

    public boolean removeOffer(UUID id) {
        JobOffer o = jobOffers.remove(id);
        if (o == null) return false;

        // Retirer des employeurs
        Employer emp = employers.get(o.getEmployerId());
        if (emp != null) emp.removeJobOfferId(id);

        // Retirer des companies
        Company c = companies.get(o.getCompanyId());
        if (c != null) c.removeJobOfferId(id);

        // Supprimer toutes les Applications associées
        for (UUID appId : new ArrayList<>(o.getApplicationIds())) {
            removeApplication(appId);
        }

        return true;
    }

    // === PUBLISH ===
    public JobOffer publishOffer(UUID offerId, UUID employerId) {
        JobOffer o = jobOffers.get(offerId);
        if (o == null) throw new NoSuchElementException();

        if (!employerId.equals(o.getEmployerId()))
            throw new SecurityException("Employer cannot publish another employer's offer.");

        o.setStatus(JobOfferStatus.Published);
        return o;
    }

    // === CLOSE ===
    public JobOffer closeOffer(UUID offerId, UUID employerId) {
        JobOffer o = jobOffers.get(offerId);
        if (o == null) throw new NoSuchElementException();

        if (!employerId.equals(o.getEmployerId()))
            throw new SecurityException("Employer cannot close another employer's offer.");

        o.setStatus(JobOfferStatus.Closed);
        return o;
    }

    // === REOPEN ===
    public JobOffer reopenOffer(UUID offerId, UUID employerId) {
        JobOffer o = jobOffers.get(offerId);
        if (o == null) throw new NoSuchElementException();

        if (!employerId.equals(o.getEmployerId()))
            throw new SecurityException("Employer cannot reopen another employer's offer.");

        o.setStatus(JobOfferStatus.Reopened);
        return o;
    }


    // ======================================================
    // APPLICATIONS
    // ======================================================

    public Application addApplication(Application app) {
        if (app == null) throw new IllegalArgumentException("Application cannot be null.");

        UUID id = app.getId() != null ? app.getId() : UUID.randomUUID();
        app.setId(id);

        // Valeurs par défaut
        if (app.getStatus() == null) app.setStatus(ApplicationStatus.Submitted);
        if (app.getSubmittedAt() == null) app.setSubmittedAt(LocalDateTime.now());
        if (app.getUpdatedAt() == null) app.setUpdatedAt(app.getSubmittedAt());

        applications.put(id, app);

        // Lier au JobOffer
        JobOffer offer = jobOffers.get(app.getJobOfferId());
        if (offer != null) offer.addApplicationId(id);

        // Lier à l'Applicant
        Applicant applicant = applicants.get(app.getApplicantId());
        if (applicant != null) applicant.addApplicationId(id);

        return app;
    }

    public boolean removeApplication(UUID id) {
        Application a = applications.remove(id);
        if (a == null) return false;

        // Retirer du JobOffer
        JobOffer o = jobOffers.get(a.getJobOfferId());
        if (o != null) o.removeApplicationId(id);

        // Retirer de l'Applicant
        Applicant ap = applicants.get(a.getApplicantId());
        if (ap != null) ap.removeApplicationId(id);

        return true;
    }

    public Application updateApplicationStatus(UUID id, ApplicationStatus status) {
        Application a = applications.get(id);
        if (a == null) throw new NoSuchElementException();

        a.setStatus(status);
        a.setUpdatedAt(LocalDateTime.now());
        return a;
    }
}
