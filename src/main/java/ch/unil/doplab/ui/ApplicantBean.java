package ch.unil.doplab.ui;

import ch.unil.doplab.Applicant;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named("applicantBean")
@RequestScoped
public class ApplicantBean {

    @Inject
    private ApplicationState appState;

    public List<Applicant> getAllApplicants() {
        return appState.getAllApplicants().values().stream()
                .collect(Collectors.toList());
    }
}
