package ch.unil.doplab.ui;

import ch.unil.doplab.Company;
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

    public void init() {
        if (companyId == null || companyId.isBlank()) return;
        try {
            UUID id = UUID.fromString(companyId);
            company = client.getCompany(id);
        } catch (IllegalArgumentException ignore) {
            company = null;
        }
    }

    // getters/setters
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public Company getCompany() { return company; }
}
