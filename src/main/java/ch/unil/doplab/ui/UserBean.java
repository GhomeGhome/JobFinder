package ch.unil.doplab.ui;

import ch.unil.doplab.client.JobFinderClient;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@RequestScoped
public class UserBean implements Serializable {

    @Inject
    private LoginBean loginBean;

    @Inject
    private JobFinderClient client;

    public String updateProfile() {
        boolean success = false;

        // 1. FORCE GET THE URL FROM THE HIDDEN INPUT
        String newPhotoUrl = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap().get("hiddenAvatarUrl");

        // --- APPLICANT LOGIC ---
        if (loginBean.isApplicant()) {
            // If the user selected something, force it into the object BEFORE sending
            if (newPhotoUrl != null && !newPhotoUrl.isBlank()) {
                loginBean.getLoggedApplicant().setPhotoUrl(newPhotoUrl);
            }

            success = client.updateApplicant(loginBean.getLoggedApplicant());
            // Do not fetch fresh data.
        }

        // --- EMPLOYER LOGIC ---
        else if (loginBean.isEmployer()) {
            if (newPhotoUrl != null && !newPhotoUrl.isBlank()) {
                loginBean.getLoggedEmployer().setPhotoUrl(newPhotoUrl);
            }

            success = client.updateEmployer(loginBean.getLoggedEmployer());

            if (loginBean.getLoggedCompany() != null) {
                boolean companySuccess = client.updateCompany(loginBean.getLoggedCompany());
                success = success && companySuccess;
            }
            // Do not fetch fresh data.

            // 3. CRITICAL: DO NOT fetch from server.
            // We trust the local session data.
        }

        if (success) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Profile updated."));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Update failed."));
        }
        return null;
    }
 }
