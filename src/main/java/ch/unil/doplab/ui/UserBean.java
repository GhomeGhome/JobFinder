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

        if (loginBean.isApplicant()) {
            // 1. Send applicant data
            success = client.updateApplicant(loginBean.getLoggedApplicant());

            // 2. Refresh applicant session
            if (success) {
                ch.unil.doplab.Applicant freshData = client.getApplicant(loginBean.getLoggedApplicant().getId());
                if (freshData != null) {
                    loginBean.setLoggedApplicant(freshData);
                }
            }

        } else if (loginBean.isEmployer()) {
            // 1. Update Employer Personal Info (First Name, Last Name, Photo...)
            success = client.updateEmployer(loginBean.getLoggedEmployer());

            // 2. Update Company Info (City, Description...)
            if (loginBean.getLoggedCompany() != null) {
                // We attempt to update the company too
                boolean companySuccess = client.updateCompany(loginBean.getLoggedCompany());

                // If the personal update worked AND company update worked, we call it a total success
                success = success && companySuccess;
            }

            // 3. Refresh Session
            if (success) {
                // Refresh Employer
                ch.unil.doplab.Employer freshEmployer = client.getEmployer(loginBean.getLoggedEmployer().getId());
                if (freshEmployer != null) {
                    loginBean.setLoggedEmployer(freshEmployer);
                }

                // Refresh Company
                if (loginBean.getLoggedCompany() != null) {
                    ch.unil.doplab.Company freshCompany = client.getCompany(loginBean.getLoggedCompany().getId());
                    if (freshCompany != null) {
                        loginBean.setLoggedCompany(freshCompany);
                    }
                }
            }
        }

        if (success) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Profile and Company updated."));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Update failed."));
        }
        return null;
    }
}