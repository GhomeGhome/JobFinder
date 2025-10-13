package ch.unil.doplab;

    /**
     * Représente un utilisateur de l'application JobFinder.
     */
    public class user {

        // === Attributs ===
        private int id;
        private String username;   // unique
        private String password;
        private String firstName;
        private String lastName;
        private String email;

        // === Constructeur ===
        public user(int id, String username, String password, String firstName, String lastName, String email) {
            this.id = id;
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

