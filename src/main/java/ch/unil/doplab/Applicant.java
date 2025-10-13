package ch.unil.doplab;

import java.net.URL;
import java.util.UUID;

public class Applicant extends User{
    private String contactInfo;
  //  private String location;
  //  private String bonusInfo;
  private String descriptionInfo;
  private URL cvInfo ;

    public Applicant(UUID id, String username, String password, String firstName, String lastName, String email, String contactInfo) {
        super(id, username, password, firstName, lastName, email);
        this.contactInfo = contactInfo;
        this.descriptionInfo = null;
    }
    public Applicant(String username, String password, String firstName, String lastName, String email, String contactInfo) {
        super(null, username, password, firstName, lastName, email);
        this.contactInfo = contactInfo;
        this.descriptionInfo = null;
    }

    public Applicant(UUID id, String username, String password, String firstName, String lastName, String email, String contactInfo, String descriptionInfo) {
        super(id, username, password, firstName, lastName, email);
        this.contactInfo = contactInfo;
        this.descriptionInfo = descriptionInfo;
    }
    public Applicant(String username, String password, String firstName, String lastName, String email, String contactInfo, String descriptionInfo) {
        super(null, username, password, firstName, lastName, email);
        this.contactInfo = contactInfo;
        this.descriptionInfo = descriptionInfo;
    }

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
        return super.toString() + "applicant";
    }


}
