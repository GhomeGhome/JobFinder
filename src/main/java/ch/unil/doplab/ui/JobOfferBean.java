package ch.unil.doplab.ui;

import ch.unil.doplab.Company;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.JobOfferStatus;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Named("jobOfferBean")
@RequestScoped
public class JobOfferBean {

    @Inject
    private ApplicationState appState;

    // Show all offers (no filter)
    public List<JobOffer> getAllOffers() {
        return appState.getAllOffers()
                .values()
                .stream()
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
}

