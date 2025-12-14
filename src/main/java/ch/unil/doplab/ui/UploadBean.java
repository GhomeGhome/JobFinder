package ch.unil.doplab.ui;

import ch.unil.doplab.Applicant;
import ch.unil.doplab.client.JobFinderClient;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

@Named("uploadBean")
@RequestScoped
public class UploadBean {

    @Inject
    private LoginBean loginBean;

    @Inject
    private JobFinderClient client;

    private Part cvFile;

    public String uploadCv() {
        try {
            if (cvFile == null || cvFile.getSize() == 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "No file chosen", "Please choose a PDF to upload."));
                return null;
            }
            String contentType = cvFile.getContentType();
            if (contentType == null || !contentType.toLowerCase().contains("pdf")) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unsupported file", "Please upload a PDF document."));
                return null;
            }

            // Storage folder (create if missing)
            File folder = new File(System.getProperty("user.home"), "jobfinder_uploads");
            if (!folder.exists()) folder.mkdirs();

            String filename = UUID.randomUUID() + ".pdf";
            File target = new File(folder, filename);

            try (InputStream in = cvFile.getInputStream();
                 FileOutputStream out = new FileOutputStream(target)) {
                in.transferTo(out);
            }

            // Public URL served by UploadServlet (/uploads/*)
            String publicUrl = "/jobfinder/uploads/" + filename;

            Applicant a = loginBean.getLoggedApplicant();
            if (a != null) {
                a.setCvInfo(publicUrl);
                client.updateApplicant(a);
                
                // Refresh the session with updated applicant data
                var fresh = client.getApplicant(a.getId());
                if (fresh != null) {
                    loginBean.setLoggedApplicant(fresh);
                }
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "CV uploaded", "Your CV is now linked to your profile."));
            
            // Redirect to refresh the page and show the CV
            return "applicantProfile?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload failed", "We could not upload your CV. Please try again."));
        }
        return null;
    }

    public Part getCvFile() { return cvFile; }
    public void setCvFile(Part cvFile) { this.cvFile = cvFile; }
}
