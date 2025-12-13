package ch.unil.doplab.ui;

import ch.unil.doplab.*;
import ch.unil.doplab.Company;
import ch.unil.doplab.ui.LoginBean;
import ch.unil.doplab.client.JobFinderClient; // NEW IMPORT
// import ch.unil.doplab.service.domain.ApplicationState; // REMOVE
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named("companyBean")
@RequestScoped
public class CompanyBean {

    // @Inject private ApplicationState appState; // OLD
    @Inject
    private LoginBean loginBean;
    @Inject
    private JobFinderClient client; // NEW

    public List<Company> getAllCompanies() {
        return client.getAllCompanies();
    }

    public List<Company> getCompaniesForLoggedEmployer() {
        if (loginBean.getLoggedEmployer() == null) return java.util.Collections.emptyList();
        java.util.UUID empId = loginBean.getLoggedEmployer().getId();
        java.util.UUID worksAt = loginBean.getLoggedEmployer().getCompanyId();

        return client.getAllCompanies().stream()
                .filter(c -> empId.equals(c.getOwnerEmployerId()) || (worksAt != null && worksAt.equals(c.getId())))
                .collect(Collectors.toList());
    }

    // simple search by name (contains, case-insensitive)
    public List<Company> searchByName(String query) {
        if (query == null || query.isBlank()) return java.util.Collections.emptyList();
        String q = query.toLowerCase();
        return client.getAllCompanies().stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public boolean linkEmployerToCompany(java.util.UUID companyId) {
        if (loginBean.getLoggedEmployer() == null || companyId == null) return false;
        var emp = loginBean.getLoggedEmployer();
        emp.setCompanyId(companyId);
        boolean ok = client.updateEmployer(emp);
        if (ok) {
            // refresh session company
            loginBean.setLoggedEmployer(emp);
            var company = client.getCompany(companyId);
            if (company != null) loginBean.setLoggedCompany(company);
        }
        return ok;
    }
    /**
     * NEW: Returns companies the applicant has applied to.
     */
    public List<Company> getApplicantCompanies() {
        if (!client.getClass().getName().contains("JobFinderClient")) {
            // Safety check if client isn't ready
            return java.util.Collections.emptyList();
        }

        // 1. Get current applicant ID
        if (loginBean.getLoggedApplicant() == null) return java.util.Collections.emptyList();
        java.util.UUID myId = loginBean.getLoggedApplicant().getId();

        // 2. Get all my applications
        List<ch.unil.doplab.Application> myApps = client.getAllApplications().stream()
                .filter(a -> myId.equals(a.getApplicantId()))
                .collect(Collectors.toList());

        // 3. Collect the Company IDs from those applications
        java.util.Set<java.util.UUID> myCompanyIds = myApps.stream()
                .map(app -> {
                    // Get job to find company
                    ch.unil.doplab.JobOffer offer = client.getJobOffer(app.getJobOfferId());
                    return (offer != null) ? offer.getCompanyId() : null;
                })
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 4. Return the actual Company objects
        return client.getAllCompanies().stream()
                .filter(c -> myCompanyIds.contains(c.getId()))
                .collect(Collectors.toList());
    }
}