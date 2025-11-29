package ch.unil.doplab.ui;

import ch.unil.doplab.Company;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named("companyBean")
@RequestScoped
public class CompanyBean {
    @Inject private ApplicationState appState;

    public List<Company> getAllCompanies() {
        return appState.getAllCompanies().values().stream().collect(Collectors.toList());
    }
}