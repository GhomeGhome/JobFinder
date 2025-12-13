package ch.unil.doplab.ui;

import ch.unil.doplab.Applicant;
import ch.unil.doplab.Employer;
import ch.unil.doplab.client.JobFinderClient; // NEW IMPORT
// import ch.unil.doplab.service.domain.ApplicationState; // REMOVE
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Optional;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private String username;
    private String password;
    private boolean loggedIn = false;
    private String role = "APPLICANT";

    private Employer loggedEmployer;
    private Applicant loggedApplicant;
    private ch.unil.doplab.Company loggedCompany;

    // @Inject
    // private ApplicationState applicationState; // OLD

    @Inject
    private JobFinderClient client; // NEW

    public String login() {
        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            addMessage("Username and password are required.");
            return null;
        }

        if ("EMPLOYER".equals(role)) {
            // NEW: Fetch list from API
            Optional<Employer> empOpt = client.getAllEmployers()
                    .stream()
                    .filter(e -> username.equalsIgnoreCase(e.getUsername()))
                    .findFirst();

            if (empOpt.isEmpty()) {
                addMessage("No employer found with that username.");
                return null;
            }

            Employer emp = empOpt.get();
            if (!password.equals(emp.getPassword())) {
                addMessage("Invalid password.");
                return null;
            }

            loggedEmployer = emp;
            if (loggedEmployer.getCompanyId() != null) {
                // Fetch the company details so we can edit them
                this.loggedCompany = client.getCompany(loggedEmployer.getCompanyId());
            }
            loggedApplicant = null;
            loggedIn = true;
            return "employerDashBoard?faces-redirect=true";

        } else { // APPLICANT
            // NEW: Fetch list from API
            Optional<Applicant> appOpt = client.getAllApplicants()
                    .stream()
                    .filter(a -> username.equalsIgnoreCase(a.getUsername()))
                    .findFirst();

            if (appOpt.isEmpty()) {
                addMessage("No applicant found with that username.");
                return null;
            }

            Applicant app = appOpt.get();
            if (!password.equals(app.getPassword())) {
                addMessage("Invalid password.");
                return null;
            }

            loggedApplicant = app;
            loggedEmployer = null;
            loggedIn = true;
            return "applicantJobs?faces-redirect=true";
        }
    }

    public void setLoggedApplicant(ch.unil.doplab.Applicant loggedApplicant) {
        this.loggedApplicant = loggedApplicant;
    }

    public void setLoggedEmployer(ch.unil.doplab.Employer loggedEmployer) {
        this.loggedEmployer = loggedEmployer;
    }

    public ch.unil.doplab.Company getLoggedCompany() {
        return loggedCompany;
    }

    public void setLoggedCompany(ch.unil.doplab.Company loggedCompany) {
        this.loggedCompany = loggedCompany;
    }

    public String logout() {
        loggedIn = false;
        username = null;
        password = null;
        loggedEmployer = null;
        loggedApplicant = null;
        role = "APPLICANT";
        return "login?faces-redirect=true";
    }

    private void addMessage(String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    public boolean isEmployer() {
        return "EMPLOYER".equals(role);
    }

    public boolean isApplicant() {
        return "APPLICANT".equals(role);
    }

    public Employer getLoggedEmployer() {
        return loggedEmployer;
    }

    public Applicant getLoggedApplicant() {
        return loggedApplicant;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}