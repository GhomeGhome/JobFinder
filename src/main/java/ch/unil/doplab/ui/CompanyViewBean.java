package ch.unil.doplab.ui;

import ch.unil.doplab.Company;
import ch.unil.doplab.Employer;
import ch.unil.doplab.client.JobFinderClient;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.UUID;

@Named
@ViewScoped
public class CompanyViewBean implements Serializable {

    @Inject
    private JobFinderClient client;

    private String companyId; // URL param
    private Company company;
    private Employer ownerEmployer; // Contact person

    public void init() {
        if (companyId == null || companyId.isBlank()) return;
        try {
            UUID id = UUID.fromString(companyId);
            company = client.getCompany(id);
            
            // Load owner employer if available
            if (company != null && company.getOwnerEmployerId() != null) {
                ownerEmployer = client.getEmployer(company.getOwnerEmployerId());
            }
        } catch (IllegalArgumentException ignore) {
            company = null;
            ownerEmployer = null;
        }
    }

    // getters/setters
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public Company getCompany() { return company; }
    public Employer getOwnerEmployer() { return ownerEmployer; }
}
