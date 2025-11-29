package ch.unil.doplab.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private String username;
    private String password;
    private boolean loggedIn = false;

    // This function runs when you click "Login"
    public String login() {
        // Simple check (In a real app, you would check the database/ApplicationState)
        if (username != null && !username.isBlank()) {
            loggedIn = true;
            // "jobOffers" refers to jobOffers.xhtml.
            // "faces-redirect=true" changes the URL in the browser.
            return "getJobOffers?faces-redirect=true";
        }
        return null; // Stay on page if failed
    }

    public String logout() {
        loggedIn = false;
        username = null;
        return "login?faces-redirect=true";
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isLoggedIn() { return loggedIn; }
}
