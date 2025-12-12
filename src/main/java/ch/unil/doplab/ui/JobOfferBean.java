package ch.unil.doplab.ui;

import ch.unil.doplab.Company;
import ch.unil.doplab.Employer;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.client.JobFinderClient; // NEW IMPORT
// import ch.unil.doplab.service.domain.ApplicationState; // COMMENTED OUT
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Named("jobOfferBean")
@RequestScoped
public class JobOfferBean {

    // @Inject
    // private ApplicationState appState; // COMMENTED OUT: We stop direct access

    @Inject
    private JobFinderClient client; // NEW: We use the bridge

    @Inject
    private LoginBean loginBean;

    private String searchKeyword;

    private String statusFilter;

    private List<Map<String, Object>> externalJobs = Collections.emptyList();
    // Show all offers (no filter)
    public List<JobOffer> getAllOffers() {
        /* OLD CODE:
        return appState.getAllOffers()
                .values()
                .stream()
                .collect(Collectors.toList());
        */

        // NEW CODE:
        return client.getAllJobOffers();
    }

    public List<JobOffer> getFilteredEmployerOffers() {
        List<JobOffer> base = getEmployerOffers();

        if (statusFilter == null || statusFilter.isEmpty()) {
            return base;
        }

        return base.stream()
                .filter(o -> o.getStatus() != null
                        && o.getStatus().name().equals(statusFilter))
                .collect(Collectors.toList());
    }

    public List<JobOffer> getFilteredOffers() {
        List<JobOffer> all = getAllOffers();

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

        /* OLD CODE:
        return appState.getAllOffers()
                .values()
                .stream()
                .filter(o -> employerId.equals(o.getEmployerId()))
                .collect(Collectors.toList());
        */

        // NEW CODE: Use the specialized method we added to the Client
        return client.getOffersByEmployer(employerId);
    }

    public void loadExternalJobs() {
        try {
            // use same search keyword as internal search
            String q = (searchKeyword == null) ? "" : searchKeyword.trim();
            // e.g. 5 results
            externalJobs = client.searchRemoteOk(q, 5);
        } catch (Exception e) {
            e.printStackTrace();
            externalJobs = Collections.emptyList();
        }
    }

    // call this from init() or from your existing refresh method
    @PostConstruct
    public void init() {
        // your current logic to load offers
        // ...
        loadExternalJobs();
    }

    // getter
    public List<Map<String, Object>> getExternalJobs() {
        if (externalJobs == null) {
            externalJobs = Collections.emptyList();
        }
        return externalJobs;
    }


    public String companyName(JobOffer offer) {
        UUID companyId = offer.getCompanyId();
        if (companyId == null) {
            return "No company";
        }

        try {
            // OLD CODE: Company c = appState.getCompany(companyId);

            // NEW CODE:
            Company c = client.getCompany(companyId);

            if (c == null) {
                return "Unknown company";
            }
            return c.getName();
        } catch (Exception e) {
            // Fallback if the API call fails or company not found
            return "Unknown company";
        }
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