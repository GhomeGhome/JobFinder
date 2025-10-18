package ch.unil.doplab;

import java.net.URL;
import java.util.UUID;

public class Applicant extends User{
    private String contactInfo;
  //  private String location;
  //  private String bonusInfo;
    private String descriptionInfo;
    private URL cvInfo ; // maybe change this to String instead of URL

    public Applicant() { }

    public Applicant(UUID id, String username, String password, String firstName, String lastName, String email, String contactInfo) {
        super(id, username, password, firstName, lastName, email);
        this.contactInfo = contactInfo;
        this.descriptionInfo = null;
    }
    public Applicant(String username, String password, String firstName, String lastName, String email, String contactInfo) {
        super(username, password, firstName, lastName, email);
        this.contactInfo = contactInfo;
        this.descriptionInfo = null;
    }
    /** Variante avec description. */
    public Applicant(String username, String password,
                     String firstName, String lastName, String email,
                     String contactInfo, String descriptionInfo) {
        this(username, password, firstName, lastName, email, contactInfo);
        this.descriptionInfo = descriptionInfo;
    }

    /** Variante avec ID + description. */
    public Applicant(UUID id, String username, String password,
                     String firstName, String lastName, String email,
                     String contactInfo, String descriptionInfo) {
        this(id, username, password, firstName, lastName, email, contactInfo);
        this.descriptionInfo = descriptionInfo;
    }

    // --- Getters / Setters ---
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getDescriptionInfo() { return descriptionInfo; }
    public void setDescriptionInfo(String descriptionInfo) { this.descriptionInfo = descriptionInfo; }

    public URL getCvInfo() { return cvInfo; }
    public void setCvInfo(URL cvInfo) { this.cvInfo = cvInfo; }

    // === Méthodes ===
    @Override
    public void register() {
        super.register();
    }

    @Override
    public void login() {
        super.login();
    }

    @Override
    public void logout() {
        super.logout();
    }

    @Override
    public String toString() {
        return "Applicant{username=%s, contactInfo=%s}".formatted(getUsername(), contactInfo);
    }


}
