package ch.unil.doplab.ui;

import ch.unil.doplab.Employer;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named("employerBean")
@RequestScoped
public class EmployerBean {
    @Inject
    private ApplicationState appState;

    public List<Employer> getAllEmployers() {
        return appState.getAllEmployers().values().stream().collect(Collectors.toList());
    }
}