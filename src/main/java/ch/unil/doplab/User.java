package ch.unil.doplab;

import java.util.Objects;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
     * Représente un utilisateur de l'application JobFinder.
     */
    public abstract class User {

        // === Attributs ===
        private UUID id;
        private String username;   // unique
        private String password;
        private String firstName;
        private String lastName;
        private String email;

        // === Constructeur ===

        public User() {}

        public User(UUID id, String username, String password, String firstName, String lastName, String email) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }

        // protected so that subclasses can call this constructor (super)
        protected User(String username, String password,
                       String firstName, String lastName, String email) {
            this(null, username, password, firstName, lastName, email);
        }

        // === Getters and setters ===

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        // add here: @JsonbTransient from Maven
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        // === Méthodes ===
        public void register() {
            System.out.println(username + " s'est enregistré avec succès.");
        }

        public void login() {
            System.out.println(username + " est connecté.");
        }

        public void logout() {
            System.out.println(username + " s'est déconnecté.");
        }

    @Override
    public String toString() {
        return "User is: {id=%s, username=%s firstName=%s, lastName=%s, email=%s}".formatted(id, username, firstName, lastName, email);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return  Objects.hashCode(id);
    }
}

