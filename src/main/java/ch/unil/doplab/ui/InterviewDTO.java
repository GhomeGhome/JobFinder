package ch.unil.doplab.ui;

import java.util.Date;

public class InterviewDTO {

    private Long id;
    private String jobTitle;
    private String companyName;
    private String applicantName;
    private Date scheduledAt;
    private String mode;
    private String status;
    private String locationOrLink;

    public InterviewDTO(Long id,
                        String jobTitle,
                        String companyName,
                        String applicantName,
                        Date scheduledAt,
                        String mode,
                        String status,
                        String locationOrLink) {
        this.id = id;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.applicantName = applicantName;
        this.scheduledAt = scheduledAt;
        this.mode = mode;
        this.status = status;
        this.locationOrLink = locationOrLink;
    }

    public Long getId() {
        return id;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public Date getScheduledAt() {
        return scheduledAt;
    }

    public String getMode() {
        return mode;
    }

    public String getStatus() {
        return status;
    }

    public String getLocationOrLink() {
        return locationOrLink;
    }
}
