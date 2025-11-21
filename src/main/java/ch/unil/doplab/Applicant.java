package ch.unil.doplab;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Représente un Applicant dans JobFinder.
 * Hérite des informations essentielles d'un User et ajoute ses propres données.
 */
public class Applicant extends User {

    private String contactInfo;        // Email, téléphone, etc.
    private String descriptionInfo;    // Profil du candidat
    private String cvInfo;             // CV sous forme de texte ou URL
    private List<String> skills = new ArrayList<>(); // compétences ESCO

    // ======================================================
    // CONSTRUCTEURS
    // ======================================================

    public Applicant() {}

    public Applicant(UUID id, String username, String password,
                     String firstName, String lastName, String email,
                     String contactInfo, String descriptionInfo, String cvInfo) {

        super(id, username, password, firstName, lastName, email);
        this.contactInfo = contactInfo;
        this.descriptionInfo = descriptionInfo;
        this.cvInfo = cvInfo;
    }

    public Applicant(String username, String password,
                     String firstName, String lastName, String email,
                     String contactInfo, String descriptionInfo, String cvInfo) {

        super(username, password, firstName, lastName, email);
        this.contactInfo = contactInfo;
        this.descriptionInfo = descriptionInfo;
        this.cvInfo = cvInfo;
    }


    // ======================================================
    // GETTERS / SETTERS
    // ======================================================

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getDescriptionInfo() { return descriptionInfo; }
    public void setDescriptionInfo(String descriptionInfo) { this.descriptionInfo = descriptionInfo; }

    public String getCvInfo() { return cvInfo; }
    public void setCvInfo(String cvInfo) { this.cvInfo = cvInfo; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) {
        this.skills = (skills != null) ? skills : new ArrayList<>();
    }


    // ======================================================
    // MÉTHODES UTILITAIRES
    // ======================================================

    public void addSkill(String skill) {
        if (skill != null && !skill.isBlank() && !skills.contains(skill))
            skills.add(skill);
    }

    public void removeSkill(String skill) {
        skills.remove(skill);
    }


    // ======================================================
    // OVERRIDE
    // ======================================================

    @Override
    public String toString() {
        return "Applicant{username=%s, contactInfo=%s, skills=%s}"
                .formatted(getUsername(), contactInfo, skills);
    }
}

