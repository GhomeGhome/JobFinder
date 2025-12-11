package ch.unil.doplab.ui;

import ch.unil.doplab.Applicant;
import ch.unil.doplab.Company;
import ch.unil.doplab.Employer;
import ch.unil.doplab.client.JobFinderClient;
import jakarta.faces.view.ViewScoped; // Important: Use ViewScoped, not RequestScoped
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import ch.unil.doplab.User;

@Named
@ViewScoped
public class ProfileViewBean implements Serializable {

    @Inject
    private JobFinderClient client;

    // Use String for the ID to avoid JSF conversion errors, we convert to UUID manually
    private String targetId;

    private Applicant targetApplicant;
    private Employer targetEmployer;
    private Company targetCompany;

    // --- INITIALIZATION METHODS ---

    public void initApplicant() {
        if (targetId != null && !targetId.isBlank()) {
            // 1. Convert String to UUID safely
            try {
                UUID uuid = UUID.fromString(targetId);
                // 2. Fetch data
                this.targetApplicant = client.getApplicant(uuid);
            } catch (IllegalArgumentException e) {
                // Invalid UUID string in URL
                this.targetApplicant = null;
            }
        }
    }

    public void initEmployer() {
        if (targetId != null && !targetId.isBlank()) {
            try {
                UUID uuid = UUID.fromString(targetId);
                this.targetEmployer = client.getEmployer(uuid);

                if (targetEmployer != null && targetEmployer.getCompanyId() != null) {
                    this.targetCompany = client.getCompany(targetEmployer.getCompanyId());
                }
            } catch (IllegalArgumentException e) {
                this.targetEmployer = null;
            }
        }
    }

    // --- AVATAR LOGIC ---

    public String getAvatarUrl(User user) {
        // 1. Safety check
        if (user == null) {
            return "https://ui-avatars.com/api/?name=User&background=random";
        }

        // 2. PRIORITY: If the user has a photo URL, USE IT!
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isBlank()) {
            return user.getPhotoUrl();
        }

        // 3. Fallback: Use the letters (UI Avatars)
        String cleanName = (user.getFirstName() != null) ? user.getFirstName().replace(" ", "+") : "User";
        return "https://ui-avatars.com/api/?name=" + cleanName + "&background=random&size=128";
    }

    private boolean isValidUrl(String url) {
        return url != null && !url.isBlank();
    }

    // --- GETTERS & SETTERS ---

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public Applicant getTargetApplicant() { return targetApplicant; }
    public Employer getTargetEmployer() { return targetEmployer; }
    public Company getTargetCompany() { return targetCompany; }
}