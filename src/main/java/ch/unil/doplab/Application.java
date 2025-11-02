package ch.unil.doplab;

import ch.unil.doplab.service.domain.ApplicationState;

import java.util.List;
import java.util.UUID;

public class Application {

    // === Attributs ===
    private UUID id;
    private String jobOfferId;   // unique
    private String applicantId;
    private String cvUrl; // or URL
    private String submittedAt;
    private String updatedAT;
    private ApplicationStatus status;
    private Double matchScore; // maybe Integer
    private List applicantIdList;


    public Application(UUID id, String jobOfferId, String applicantId) {
        this.id = id;
        this.jobOfferId = jobOfferId;
        this.applicantId = applicantId;

    }

    public Application(UUID id, String jobOfferId, String applicantId, String cvUrl,  String submittedAt, String updatedAT, ApplicationStatus status) {
        this.id = id;
        this.jobOfferId = jobOfferId;
        this.applicantId = applicantId;
        this.cvUrl = cvUrl;
        this.submittedAt = submittedAt;
        this.updatedAT = updatedAT;
        this.status = status;

    }

    public void Application(String applicantId) {
// send the applicant's ID --> employer can search the applicant and display his info
        this.applicantIdList.add(applicantId);
    }

}


