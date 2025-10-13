package ch.unil.doplab;

import java.net.URL;
import java.util.UUID;

public class Employer extends User{
    private String enterpriseName;
 //   private String applicantId;

    public Employer(UUID id, String username, String password, String firstName, String lastName, String email, String enterpriseName) {
        super(id, username, password, firstName, lastName, email);
        this.enterpriseName = enterpriseName;
    }

    // For JobOffer --> has to have an ID

 //   public Employer(String username, String password, String firstName, String lastName, String email, String enterpriseName) {
 //       super(null, username, password, firstName, lastName, email);
  //      this.enterpriseName = enterpriseName;
 //   }

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
        return super.toString() + "employer";
    }

}