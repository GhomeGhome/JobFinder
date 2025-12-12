package ch.unil.doplab.service.domain;

import ch.unil.doplab.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;



/**
 * ApplicationState est le "stockage mémoire" central de JobFinder.
 * Il gère toutes les entités : Employers, Applicants, Companies,
 * JobOffers, Applications.
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

    @PostConstruct
    public void init() {
        employers  = new HashMap<>();
        applicants = new HashMap<>();
        companies  = new HashMap<>();
        jobOffers  = new HashMap<>();
        applications = new HashMap<>();

        // Load existing DB content into the maps (if any)
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        // 1) load base entities
        for (Employer e : em.createQuery("SELECT e FROM Employer e", Employer.class).getResultList()) {
            employers.put(e.getId(), e);
        }

        for (Applicant a : em.createQuery("SELECT a FROM Applicant a", Applicant.class).getResultList()) {
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

        // 2) rebuild in-memory “inverse” relations, if you want:
        //    - company.employerIds, company.jobOfferIds
        //    - employer.jobOfferIds
        //    - jobOffer.applicationIds
        //    - applicant.applicationIds

        for (JobOffer o : jobOffers.values()) {
            UUID empId = o.getEmployerId();
            if (empId != null) {
                Employer e = employers.get(empId);
                if (e != null) e.addJobOfferId(o.getId());
            }

            UUID companyId = o.getCompanyId();
            if (companyId != null) {
                Company c = companies.get(companyId);
                if (c != null) c.addJobOfferId(o.getId());
            }
        }

        for (Application app : applications.values()) {
            JobOffer offer = jobOffers.get(app.getJobOfferId());
            if (offer != null) {
                offer.addApplicationId(app.getId());
            }
            Applicant applicant = applicants.get(app.getApplicantId());
            if (applicant != null) {
                applicant.addApplicationId(app.getId());
            }
        }

        // (Optionally link company ↔ owner employer)
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
    }

    // ... keep your existing getters like getAllEmployers(), getEmployer(id), etc.

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
        addApplicant(igor);

        Applicant sara = new Applicant();
        sara.setFirstName("Sara");
        sara.setLastName("Novak");
        sara.setEmail("sara@example.com");
        sara.setUsername("sara");
        sara.setPassword("sara123");
        addApplicant(sara);

        Applicant tom = new Applicant();
        tom.setFirstName("Tom");
        tom.setLastName("Candidate");
        tom.setEmail("tom@example.com");
        tom.setUsername("tom");
        tom.setPassword("tom123");
        addApplicant(tom);

        Applicant emma = new Applicant();
        emma.setFirstName("Emma");
        emma.setLastName("Liu");
        emma.setEmail("emma@example.com");
        emma.setUsername("emma");
        emma.setPassword("emma123");
        addApplicant(emma);

        Applicant marc = new Applicant();
        marc.setFirstName("Marc");
        marc.setLastName("Dubois");
        marc.setEmail("marc@example.com");
        marc.setUsername("marc");
        marc.setPassword("marc123");
        addApplicant(marc);

        Applicant julia = new Applicant();
        julia.setFirstName("Julia");
        julia.setLastName("Rossi");
        julia.setEmail("julia@example.com");
        julia.setUsername("julia");
        julia.setPassword("julia123");
        addApplicant(julia);

        Applicant nico = new Applicant();
        nico.setFirstName("Nicolas");
        nico.setLastName("Keller");
        nico.setEmail("nicolas@example.com");
        nico.setUsername("nicolas");
        nico.setPassword("nico123");
        addApplicant(nico);

        Applicant lina = new Applicant();
        lina.setFirstName("Lina");
        lina.setLastName("Müller");
        lina.setEmail("lina@example.com");
        lina.setUsername("lina");
        lina.setPassword("lina123");
        addApplicant(lina);

        Applicant omar = new Applicant();
        omar.setFirstName("Omar");
        omar.setLastName("Haddad");
        omar.setEmail("omar@example.com");
        omar.setUsername("omar");
        omar.setPassword("omar123");
        addApplicant(omar);

        Applicant chloe = new Applicant();
        chloe.setFirstName("Chloé");
        chloe.setLastName("Morel");
        chloe.setEmail("chloe@example.com");
        chloe.setUsername("chloe");
        chloe.setPassword("chloe123");
        addApplicant(chloe);

        // ========= JOB OFFERS =========
        // Use your JobOfferStatus enum: adjust names if different (e.g. PUBLISHED/DRAFT/CLOSED)
        JobOffer o1 = new JobOffer();
        o1.setTitle("Junior Java Developer");
        o1.setDescription("Work on backend services in Java for our web platform.");
        o1.setEmployerId(alice.getId());
        o1.setCompanyId(acme.getId());
        o1.setStatus(JobOfferStatus.Published);
        addOffer(o1);

        JobOffer o2 = new JobOffer();
        o2.setTitle("Data Analyst Intern");
        o2.setDescription("Help analyse job market and platform usage data.");
        o2.setEmployerId(bob.getId());
        o2.setCompanyId(dataWorks.getId());
        o2.setStatus(JobOfferStatus.Published);
        addOffer(o2);

        JobOffer o3 = new JobOffer();
        o3.setTitle("DevOps Engineer");
        o3.setDescription("Maintain CI/CD pipelines and cloud infrastructure.");
        o3.setEmployerId(carla.getId());
        o3.setCompanyId(greenFuture.getId());
        o3.setStatus(JobOfferStatus.Draft);
        addOffer(o3);

        JobOffer o4 = new JobOffer();
        o4.setTitle("Frontend Developer");
        o4.setDescription("Build and style the JobFinder web UI.");
        o4.setEmployerId(alice.getId());
        o4.setCompanyId(acme.getId());
        o4.setStatus(JobOfferStatus.Published);
        addOffer(o4);

        JobOffer o5 = new JobOffer();
        o5.setTitle("Machine Learning Engineer");
        o5.setDescription("Develop matching and recommendation models.");
        o5.setEmployerId(bob.getId());
        o5.setCompanyId(dataWorks.getId());
        o5.setStatus(JobOfferStatus.Closed);
        addOffer(o5);

        JobOffer o6 = new JobOffer();
        o6.setTitle("Sustainability Consultant");
        o6.setDescription("Advise clients on green transformation projects.");
        o6.setEmployerId(carla.getId());
        o6.setCompanyId(greenFuture.getId());
        o6.setStatus(JobOfferStatus.Published);
        addOffer(o6);

        JobOffer o7 = new JobOffer();
        o7.setTitle("Product Manager – Fintech");
        o7.setDescription("Own product roadmap for a digital payment solution.");
        o7.setEmployerId(bob.getId());
        o7.setCompanyId(finEdge.getId());
        o7.setStatus(JobOfferStatus.Published);
        addOffer(o7);

        JobOffer o8 = new JobOffer();
        o8.setTitle("Mobile Developer (iOS/Android)");
        o8.setDescription("Develop our health monitoring mobile app.");
        o8.setEmployerId(david.getId());
        o8.setCompanyId(healthSync.getId());
        o8.setStatus(JobOfferStatus.Draft);
        addOffer(o8);

        JobOffer o9 = new JobOffer();
        o9.setTitle("Robotics Engineer Intern");
        o9.setDescription("Support development of warehouse robotics projects.");
        o9.setEmployerId(alice.getId());
        o9.setCompanyId(roboLabs.getId());
        o9.setStatus(JobOfferStatus.Published);
        addOffer(o9);

        JobOffer o10 = new JobOffer();
        o10.setTitle("Cloud Architect");
        o10.setDescription("Design scalable cloud architectures for enterprise clients.");
        o10.setEmployerId(david.getId());
        o10.setCompanyId(cloudNova.getId());
        o10.setStatus(JobOfferStatus.Published);
        addOffer(o10);

        JobOffer o11 = new JobOffer();
        o11.setTitle("UX/UI Designer");
        o11.setDescription("Design intuitive interfaces for learning platforms.");
        o11.setEmployerId(alice.getId());
        o11.setCompanyId(eduTech.getId());
        o11.setStatus(JobOfferStatus.Draft);
        addOffer(o11);

        JobOffer o12 = new JobOffer();
        o12.setTitle("Operations Manager");
        o12.setDescription("Oversee logistics operations and process improvement.");
        o12.setEmployerId(bob.getId());
        o12.setCompanyId(alpineLogistics.getId());
        o12.setStatus(JobOfferStatus.Published);
        addOffer(o12);

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

    @Transactional
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

        em.persist(e);

        return e;
    }

    @Transactional
    public boolean setEmployer(UUID id, Employer updated) {
        Employer existing = em.find(Employer.class, id);
        if (existing == null) {
            return false;
        }

        updated.setId(id);

        // preserve relationship if not sent by client
        if (updated.getCompanyId() == null) {
            updated.setCompanyId(existing.getCompanyId());
        }

        // write to DB
        Employer merged = em.merge(updated);

        // update RAM cache
        employers.put(id, merged);

        return true;
    }


    public boolean removeEmployer(UUID id) {
        Employer emp = employers.remove(id);
        if (emp == null) {
            return false;
        }

        // Detach from company if any
        UUID companyId = emp.getCompanyId();
        if (companyId != null) {
            Company c = companies.get(companyId);
            if (c != null) {
                c.removeEmployerId(id);
                if (id.equals(c.getOwnerEmployerId())) {
                    c.setOwnerEmployerId(null);
                }
            }
        }

        // (Optionally handle jobOffers created by this employer – you can leave as-is for now)

        return true;
    }


    // ======================================================
    // APPLICANTS
    // ======================================================

    @Transactional
    public Applicant addApplicant(Applicant a) {
        if (a == null) throw new IllegalArgumentException("Applicant cannot be null.");

        UUID id = a.getId() != null ? a.getId() : UUID.randomUUID();
        a.setId(id);

        applicants.put(id, a);
        em.persist(a);
        return a;
    }

    @Transactional
    public boolean setApplicant(UUID id, Applicant updated) {
        // 1) Check existence in DB
        Applicant existing = em.find(Applicant.class, id);
        if (existing == null) {
            return false;
        }

        // 2) Enforce ID
        updated.setId(id);

        // 3) Merge into DB
        Applicant merged = em.merge(updated);

        // 4) Update in-memory cache
        applicants.put(id, merged);

        return true;
    }

    @Transactional
    public boolean removeApplicant(UUID id) {
        Applicant ap = applicants.remove(id);
        if (ap == null) {
            return false;
        }

        // Remove all applications of this applicant (from RAM only – OK for demo)
        for (UUID appId : new ArrayList<>(ap.getApplicationIds())) {
            removeApplication(appId);
        }

        // Remove from DB
        Applicant managed = em.find(Applicant.class, id);
        if (managed != null) {
            em.remove(managed);
        }

        return true;
    }

    // ======================================================
    // COMPANIES
    // ======================================================

    @Transactional
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

        em.persist(c);

        return c;
    }

    @Transactional
    public boolean setCompany(UUID id, Company updated) {
        Company existing = em.find(Company.class, id);
        if (existing == null) {
            return false;
        }

        updated.setId(id);

        if (updated.getOwnerEmployerId() == null) {
            updated.setOwnerEmployerId(existing.getOwnerEmployerId());
        }

        // write to DB
        Company merged = em.merge(updated);

        // update RAM cache
        companies.put(id, merged);

        return true;
    }

    public boolean removeCompany(UUID id) {
        Company c = companies.remove(id);
        if (c == null) {
            return false;
        }

        // Detach from employers
        for (UUID empId : new ArrayList<>(c.getEmployerIds())) {
            Employer e = employers.get(empId);
            if (e != null && id.equals(e.getCompanyId())) {
                e.setCompanyId(null);
            }
            c.removeEmployerId(empId);
        }

        // Detach from job offers
        for (UUID offerId : new ArrayList<>(c.getJobOfferIds())) {
            JobOffer o = jobOffers.get(offerId);
            if (o != null) {
                o.setCompanyId(null);
            }
            c.removeJobOfferId(offerId);
        }

        return true;
    }

    // ======================================================
    // JOB OFFERS
    // ======================================================

    @Transactional
    public JobOffer addOffer(JobOffer o) {
        if (o == null) throw new IllegalArgumentException("JobOffer cannot be null.");
        if (o.getEmployerId() == null) throw new IllegalArgumentException("JobOffer must have employerId");

        UUID id = o.getId() != null ? o.getId() : UUID.randomUUID();
        o.setId(id);

        // Valeurs par défaut
        if (o.getStatus() == null) o.setStatus(JobOfferStatus.Draft);
        if (o.getCreatedAt() == null) o.setCreatedAt(LocalDateTime.now());

        // 1) update RAM
        jobOffers.put(id, o);

        Employer emp = employers.get(o.getEmployerId());
        if (emp != null) emp.addJobOfferId(id);

        if (o.getCompanyId() != null) {
            Company c = companies.get(o.getCompanyId());
            if (c != null) c.addJobOfferId(id);
        }

        // 2) persist to DB
        em.persist(o);

        return o;
    }

    public boolean setOffer(UUID id, JobOffer updated) {
        JobOffer existing = jobOffers.get(id);
        if (existing == null) return false;

        updated.setId(id);
        jobOffers.put(id, updated);
        return true;
    }

    @Transactional
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

        JobOffer managed = em.find(JobOffer.class, id);
        if(managed != null){
            em.remove(managed);
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

    @Transactional
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

        em.persist(app);

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

    @Transactional
    public Application updateApplicationStatus(UUID id, ApplicationStatus status) {
        Application a = applications.get(id);
        if (a == null) throw new NoSuchElementException();

        a.setStatus(status);
        a.setUpdatedAt(LocalDateTime.now());

        // sync DB
        Application managed = em.find(Application.class, id);
        if (managed != null) {
            managed.setStatus(status);
            managed.setUpdatedAt(a.getUpdatedAt());
        }

        return a;
    }

    // ======================================================
    // INTERVIEWS
    // ======================================================

//    public Map<UUID, Interview> getAllInterviews() {
//        return interviews;
//    }
//
//    public void addInterview(Interview interview) {
//        interviews.put(interview.getId(), interview);
//    }
//
//    public List<Interview> getInterviewsForEmployer(UUID employerId) {
//        return interviews.values().stream()
//                .filter(i -> employerId.equals(i.getEmployerId()))
//                .collect(Collectors.toList());
//    }
    // ======================================================
    // NEW METHODS FOR JSF BEANS
    // ======================================================

    /**
     * For Igor's "My Applications" page.
     */
    public List<Application> getApplicationsByApplicantId(UUID applicantId) {
        return applications.values().stream()
                .filter(app -> app.getApplicantId().equals(applicantId))
                .collect(Collectors.toList());
    }

    /**
     * For Employer's "View Applicants" button on a specific job.
     */
    public List<Application> getApplicationsByJobId(UUID jobId) {
        return applications.values().stream()
                .filter(app -> app.getJobOfferId().equals(jobId))
                .collect(Collectors.toList());
    }

    /**
     * For Employer's "All Applications" page.
     */
    public List<Application> getApplicationsByEmployerId(UUID employerId) {
        // 1. Find all job IDs owned by this employer
        List<UUID> employerJobIds = jobOffers.values().stream()
                .filter(job -> job.getEmployerId().equals(employerId))
                .map(JobOffer::getId)
                .collect(Collectors.toList());

        // 2. Return applications that match any of those Job IDs
        return applications.values().stream()
                .filter(app -> employerJobIds.contains(app.getJobOfferId()))
                .collect(Collectors.toList());
    }

    /**
     * Helper to get a Job Title by ID.
     */
    public String getJobTitleById(UUID jobId) {
        JobOffer offer = jobOffers.get(jobId);
        return (offer != null) ? offer.getTitle() : "Unknown Job";
    }

    /**
     * Helper to get an Applicant Name by ID.
     */
    public String getApplicantNameById(UUID applicantId) {
        Applicant applicant = applicants.get(applicantId);
        return (applicant != null) ? applicant.getFirstName() + " " + applicant.getLastName() : "Unknown";
    }

    /**
     * Helper to get Company Name by Job ID
     */
    public String getCompanyNameByJobId(UUID jobId) {
        JobOffer offer = jobOffers.get(jobId);
        if (offer == null) return "Unknown";
        Company c = companies.get(offer.getCompanyId());
        return (c != null) ? c.getName() : "Unknown";
    }
@Transactional
public void clearDB() {
    // clear RAM first
    clearObjects();

    // delete children first, then parents
    em.createQuery("DELETE FROM Application").executeUpdate();
    em.createQuery("DELETE FROM JobOffer").executeUpdate();
    em.createQuery("DELETE FROM Applicant").executeUpdate();
    em.createQuery("DELETE FROM Employer").executeUpdate();
    em.createQuery("DELETE FROM Company").executeUpdate();
}

    @Transactional
    public void populateDB() {
        // full reset, like StudyBuddy
        clearDB();                  // clears RAM + DB tables
        populateApplicationState(); // uses addEmployer/addCompany/addApplicant/addOffer/addApplication
    }

    @Transactional
    public void resetDB() {
        // just delegate to populateDB for clarity
        populateDB();
    }
}
