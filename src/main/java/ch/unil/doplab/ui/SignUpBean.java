package ch.unil.doplab.ui;

import ch.unil.doplab.Applicant;
import ch.unil.doplab.Employer;
import ch.unil.doplab.client.JobFinderClient;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named("signUpBean")
@RequestScoped
public class SignUpBean implements Serializable {

    @Inject
    private JobFinderClient client;

    // role + common fields
    private String role = "APPLICANT";
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;

    // employer-specific
    private String enterpriseName;

    public String signUp() {
        boolean ok = false;
        try {
            if ("EMPLOYER".equals(role)) {
                Employer e = new Employer();
                e.setFirstName(firstName);
                e.setLastName(lastName);
                e.setUsername(username);
                e.setPassword(password);
                e.setEmail(email);
                e.setEnterpriseName(enterpriseName);
                ok = client.createEmployer(e) != null;
            } else {
                Applicant a = new Applicant();
                a.setFirstName(firstName);
                a.setLastName(lastName);
                a.setUsername(username);
                a.setPassword(password);
                a.setEmail(email);
                ok = client.createApplicant(a) != null;
            }
        } catch (Exception e) {
            ok = false;
        }

        if (ok) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Account created. Please sign in."));
            return "login?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not create account."));
            return null;
        }
    }

    // getters/setters
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEnterpriseName() { return enterpriseName; }
    public void setEnterpriseName(String enterpriseName) { this.enterpriseName = enterpriseName; }
}
