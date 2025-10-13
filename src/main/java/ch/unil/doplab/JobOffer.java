package ch.unil.doplab;

import java.util.List;
import java.util.UUID;

public class JobOffer {

    // === Attributs ===
    private UUID id;
    private String employerID;   // unique
    private String title;
    private String description;
    private String employmentType;
    private String status;
    private String startDate;
    private String endDate;
    private List requiredQualifications;
    private List requiredSkills;
    private List applicantIdList;

    // === Constructeur ===
    public JobOffer(UUID id, String employerID, String title) {
        this.id = id;
        this.employerID = employerID;
        this.title = title;
}
    public JobOffer(UUID id, String employerID, String title, String description, String employmentType, String status, String startDate, String endDate) {
        this.id = id;
        this.employerID = employerID;
        this.title = title;
        this.description = description;
        this.employmentType = employmentType;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // === Méthodes ===
    public void requiredSkills(String skills) {
       this.requiredSkills.add(skills);
    }

    public void requiredQualifications(String qualifs) {
        this.requiredQualifications.add(qualifs);
    }

    public void Application(String applicantID) {
// send the applicant's ID --> employer can search the applicant and display his info
       this.applicantIdList.add(applicantID);
    }
}
