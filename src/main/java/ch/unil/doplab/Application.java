package ch.unil.doplab;

import java.util.List;
import java.util.UUID;

public class Application {

    // === Attributs ===
    private UUID id;
    private String jobOfferId;   // unique
    private String applicantId;
    private String cvUrl;
    private String submittedAt;
    private String updatedAT;
    private String status;
    private String matchScore;
    private String applicantID;
    private List applicantIdList;


    public Application(UUID id, String jobOfferId, String applicantId) {
        this.id = id;
        this.jobOfferId = jobOfferId;
        this.applicantId = applicantId;

    }

    public Application(UUID id, String jobOfferId, String applicantId, String cvUrl,  String submittedAt, String updatedAT, String status) {
        this.id = id;
        this.jobOfferId = jobOfferId;
        this.applicantId = applicantId;
        this.cvUrl = cvUrl;
        this.submittedAt = submittedAt;
        this.updatedAT = updatedAT;
        this.status = status;

    }

    public void Application(String applicantID) {
// send the applicant's ID --> employer can search the applicant and display his info
        this.applicantIdList.add(applicantID);
    }

}


