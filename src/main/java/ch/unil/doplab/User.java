package ch.unil.doplab;

import java.util.UUID;

/**
     * Représente un utilisateur de l'application JobFinder.
     */
    public class User {

        // === Attributs ===
        private UUID id;
        private String username;   // unique
        private String password;
        private String firstName;
        private String lastName;
        private String email;

        // === Constructeur ===
        public User(UUID id, String username, String password, String firstName, String lastName, String email) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }
    public User(String username, String password, String firstName, String lastName, String email) {
        this.id = null;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

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

    }

