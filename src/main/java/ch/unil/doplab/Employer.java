package ch.unil.doplab;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Représente un employeur dans JobFinder.
 * Il hérite de User et ajoute des informations propres à l'entreprise.
 */
@Entity
@Table(name = "employers")
public class Employer extends User {

    private String descriptionInfo;

    @Column(length = 255)
    private String enterpriseName;
    @Column(name = "company_id")
    private UUID companyId;   // lien vers la Company représentée

    // ======================================================
    // CONSTRUCTEURS
    // ======================================================

    public Employer() {}

    public Employer(UUID id, String username, String password,
                    String firstName, String lastName, String email,
                    String enterpriseName, UUID companyId) {

        super(id, username, password, firstName, lastName, email);
        this.enterpriseName = enterpriseName;
        this.companyId = companyId;
    }

    public Employer(String username,
                    String password,
                    String firstName,
                    String lastName,
                    String email,
                    String enterpriseName,
                    UUID companyId) {

        this(null, username, password, firstName, lastName, email,
                enterpriseName, companyId);
    }


    // ======================================================
    // GETTERS / SETTERS
    // ======================================================

    public String getEnterpriseName() { return enterpriseName; }
    public void setEnterpriseName(String enterpriseName) { this.enterpriseName = enterpriseName; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public String getDescriptionInfo() {
        return descriptionInfo;
    }

    public void setDescriptionInfo(String descriptionInfo) {
        this.descriptionInfo = descriptionInfo;
    }

    // ======================================================
    // OVERRIDES
    // ======================================================

    @Override
    public String toString() {
        return "Employer{username=%s, enterpriseName=%s, companyId=%s}"
                .formatted(getUsername(), enterpriseName, companyId);
    }

    @Override
    public void setName(String first_name, String last_name){
        super.setName(first_name, last_name);
    }
}
