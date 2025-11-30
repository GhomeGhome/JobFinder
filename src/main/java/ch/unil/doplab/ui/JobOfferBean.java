package ch.unil.doplab.ui;

import ch.unil.doplab.Company;
import ch.unil.doplab.Employer;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.JobOfferStatus;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Named("jobOfferBean")
@RequestScoped
public class JobOfferBean {

    @Inject
    private ApplicationState appState;

    @Inject
    private LoginBean loginBean;

    private String searchKeyword;

    private String statusFilter;

    // Show all offers (no filter)
    public List<JobOffer> getAllOffers() {
        return appState.getAllOffers()
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    public List<JobOffer> getFilteredEmployerOffers() {
        List<JobOffer> base = getEmployerOffers(); // your existing method

        if (statusFilter == null || statusFilter.isEmpty()) {
            return base;
        }

        return base.stream()
                .filter(o -> o.getStatus() != null
                        && o.getStatus().name().equals(statusFilter))
                .collect(Collectors.toList());
    }

    public List<JobOffer> getFilteredOffers() {
        List<JobOffer> all = getAllOffers(); // or whatever your getter is

        if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
            return all;
        }

        String kw = searchKeyword.toLowerCase();
        List<JobOffer> result = new ArrayList<>();

        for (JobOffer o : all) {
            String title = o.getTitle() != null ? o.getTitle().toLowerCase() : "";
            String description = o.getDescription() != null ? o.getDescription().toLowerCase() : "";

            // companyName(...) is the helper you already use in your pages:
            // #{jobOfferBean.companyName(offer)}
            String companyName = companyName(o);
            String company = companyName != null ? companyName.toLowerCase() : "";

            if (title.contains(kw) || description.contains(kw) || company.contains(kw)) {
                result.add(o);
            }
        }

        return result;
    }

    public List<JobOffer> getEmployerOffers() {
        // if not logged in as employer, nothing to show
        if (!loginBean.isEmployer()) {
            return new ArrayList<>();
        }

        Employer current = loginBean.getLoggedEmployer();
        if (current == null || current.getId() == null) {
            return new ArrayList<>();
        }

        UUID employerId = current.getId();

        return appState.getAllOffers()
                .values()
                .stream()
                .filter(o -> employerId.equals(o.getEmployerId()))
                .collect(Collectors.toList());
    }

    public String companyName(JobOffer offer) {
        UUID companyId = offer.getCompanyId();
        if (companyId == null) {
            return "No company";
        }
        Company c = appState.getCompany(companyId);
        if (c == null) {
            return "Unknown company";
        }
        return c.getName();
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }
}

