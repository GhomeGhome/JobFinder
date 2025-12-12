package ch.unil.doplab;

import jakarta.persistence.*;

import java.util.*;

/**
 * Représente un utilisateur générique (Employeur ou Applicant)
 * dans l’application JobFinder.
 */
@MappedSuperclass
public abstract class User {

    // ======================================================
    // ATTRIBUTS
    // ======================================================

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;     // unique
    @Column(name = "password", length = 255)
    private String password;
    @Column(name = "first_name", length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(name = "email", length = 255)
    private String email;

    // Un User peut être lié à plusieurs JobOffers (Employeur)
    @Transient
    protected List<UUID> jobOfferIds = new ArrayList<>();

    // Un User peut être lié à plusieurs Applications (Applicant)
    @Transient
    protected List<UUID> applicationIds = new ArrayList<>();


    // ======================================================
    // CONSTRUCTEURS
    // ======================================================

    public User() {}

    public User(UUID id, String username, String password,
                String firstName, String lastName, String email) {

        this.id = id;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    protected User(String username, String password,
                   String firstName, String lastName, String email) {
        this(null, username, password, firstName, lastName, email);
    }

    @PrePersist
    protected void ensureId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    // ======================================================
    // GETTERS / SETTERS
    // ======================================================

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // @JsonbTransient pourrait être ajouté si nécessaire
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }


    // ======================================================
    // JobOffer IDs (pour Employeur)
    // ======================================================

    public List<UUID> getJobOfferIds() { return jobOfferIds; }

    public void addJobOfferId(UUID id) {
        if (id != null && !jobOfferIds.contains(id))
            jobOfferIds.add(id);
    }

    public void removeJobOfferId(UUID id) {
        jobOfferIds.remove(id);
    }


    // ======================================================
    // Application IDs (pour Applicant)
    // ======================================================

    public List<UUID> getApplicationIds() { return applicationIds; }

    public void addApplicationId(UUID id) {
        if (id != null && !applicationIds.contains(id))
            applicationIds.add(id);
    }

    public void removeApplicationId(UUID id) {
        applicationIds.remove(id);
    }


    // ======================================================
    // MÉTHODES FONCTIONNELLES
    // ======================================================

    public void register() {
        System.out.println(username + " s'est enregistré avec succès.");
    }

    public void login() {
        System.out.println(username + " est connecté.");
    }

    public void logout() {
        System.out.println(username + " s'est déconnecté.");
    }

    public void updatePersonalInfo(String firstName, String lastName, String email) {
        if (firstName != null && !firstName.isBlank()) this.firstName = firstName;
        if (lastName != null && !lastName.isBlank()) this.lastName = lastName;
        if (email != null && !email.isBlank()) this.email = email;
    }


    // ======================================================
    // OVERRIDES
    // ======================================================

    @Override
    public String toString() {
        return "User{id=%s, username=%s, firstName=%s, lastName=%s, email=%s}"
                .formatted(id, username, firstName, lastName, email);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void setName(String first_name, String last_name){
        firstName = first_name;
        lastName = last_name;
    }
}
