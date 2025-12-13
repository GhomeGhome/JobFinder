package ch.unil.doplab.ui;

import ch.unil.doplab.Application;
import ch.unil.doplab.Applicant;
import ch.unil.doplab.JobOffer;
import ch.unil.doplab.Employer;
import ch.unil.doplab.client.JobFinderClient;

import java.util.*;
import java.util.ArrayList;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.stream.Collectors;

@Named("applicationBean")
@SessionScoped
public class ApplicationBean implements Serializable {

    // @Inject
    // private ApplicationState appState; // OLD

    @Inject
    private JobFinderClient client; // NEW

    @Inject
    private LoginBean loginBean;

    private UUID filterJobOfferId;

    // --- Actions ---

    public String updateStatus(ch.unil.doplab.Application app) {
        if (app != null) {
            try {
                client.updateApplicationStatus(app.getId(), app.getStatus().name());

                // Optional: refresh scores or employer cache (not strictly needed for status)
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Status updated to " + app.getStatus()));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not update status."));
            }
        }
        return null;
    }

    /**
     * Called when clicking "View applicants" on the dashboard.
     */
    public String openApplicationsForJob(UUID offerId) {
        this.filterJobOfferId = offerId;
        // Navigate to the applications page
        return "employerApplications?faces-redirect=true&includeViewParams=true";
    }

    public String clearJobFilter() {
        this.filterJobOfferId = null;
        // Reload page without filter
        return "employerApplications?faces-redirect=true";
    }

    // --- Data Access ---

    public List<Application> getAllApplications() {
        return client.getAllApplications();
    }

    /**
     * Main list for 'employerApplications.xhtml'.
     * Filters: 1. By Employer Owner, 2. By Specific Job (if selected).
     */
    public List<Application> getApplicationsForLoggedEmployer() {
        // Security check
        if (!loginBean.isEmployer() || loginBean.getLoggedEmployer() == null) {
            return Collections.emptyList();
        }

        UUID employerId = loginBean.getLoggedEmployer().getId();

        // 1. Fetch EVERYTHING from server
        List<Application> allApps = client.getAllApplications();

        List<Application> result = new ArrayList<>();

        for (Application app : allApps) {
            // Fetch the job for this application to check ownership
            JobOffer offer = client.getJobOffer(app.getJobOfferId());
            if (offer == null) continue; // Orphaned application

            // CHECK A: Is this job owned by the logged-in employer?
            if (!employerId.equals(offer.getEmployerId())) {
                continue;
            }

            // CHECK B: Are we filtering by a specific job ID?
            if (filterJobOfferId != null && !filterJobOfferId.equals(offer.getId())) {
                continue;
            }

            // --- compute + persist match score (always) ---
            Applicant applicant = client.getApplicant(app.getApplicantId());
            if (applicant != null) {
                double score = computeMatchScore(applicant, offer);

                // Persist only if missing or significantly changed
                Double current = app.getMatchScore();
                if (current == null || Math.abs(current - score) > 0.05) {
                    app.setMatchScore(score); // immediate UI update
                    client.updateApplicationMatchScore(app.getId(), score); // save to DB/API
                }
            }

            result.add(app);
        }

        return result;
    }

    // --- Helpers for UI ---

    public String getJobTitle(UUID offerId) {
        if (offerId == null) return "Unknown";
        JobOffer offer = client.getJobOffer(offerId);
        return (offer != null) ? offer.getTitle() : "Offer Removed";
    }

    public String getApplicantName(UUID applicantId) {
        if (applicantId == null) return "Unknown";
        Applicant applicant = client.getApplicant(applicantId);
        return (applicant != null)
                ? applicant.getFirstName() + " " + applicant.getLastName()
                : "User Removed";
    }

    // -----------------------------
// Matching helpers (UI fallback)
// -----------------------------
    private static Set<String> tokenize(String text) {
        if (text == null) return Collections.emptySet();

        String[] raw = text
                .toLowerCase()
                .split("[^a-z0-9+]+");

        Set<String> tokens = new HashSet<>();
        for (String t : raw) {
            t = t.trim();
            if (t.length() >= 2) {
                tokens.add(t);
            }
        }
        return tokens;
    }

    private static double phraseSimilarity(String a, String b) {
        if (a == null || b == null) return 0.0;
        String sa = a.trim().toLowerCase();
        String sb = b.trim().toLowerCase();
        if (sa.isEmpty() || sb.isEmpty()) return 0.0;

        if (sa.equals(sb)) return 1.0;
        if (sa.contains(sb) || sb.contains(sa)) return 0.7;

        Set<String> ta = tokenize(sa);
        Set<String> tb = tokenize(sb);
        if (ta.isEmpty() || tb.isEmpty()) return 0.0;
        Set<String> inter = new HashSet<>(ta);
        inter.retainAll(tb);
        Set<String> union = new HashSet<>(ta);
        union.addAll(tb);
        return union.isEmpty() ? 0.0 : (inter.size() * 1.0) / union.size();
    }

    private static double listSimilarity(Collection<String> reqs, Collection<String> phrases) {
        if (reqs == null || reqs.isEmpty() || phrases == null || phrases.isEmpty()) return 0.0;
        double sum = 0.0;
        int n = 0;
        for (String r : reqs) {
            if (r == null || r.isBlank()) continue;
            double best = 0.0;
            for (String p : phrases) {
                best = Math.max(best, phraseSimilarity(r, p));
                if (best >= 1.0) break;
            }
            sum += best;
            n++;
        }
        if (n == 0) return 0.0;
        return (sum / n) * 100.0;
    }

    private double computeMatchScore(Applicant applicant, JobOffer offer) {
        if (applicant == null || offer == null) return 0.0;

        LinkedHashSet<String> applicantPhrases = new LinkedHashSet<>();
        if (applicant.getSkills() != null) {
            for (String s : applicant.getSkills()) {
                if (s != null && !s.isBlank()) applicantPhrases.add(s.trim().toLowerCase());
            }
        }
        if (applicantPhrases.isEmpty()) {
            String skillsStr = applicant.getSkillsAsString();
            if (skillsStr != null && !skillsStr.isBlank()) {
                for (String s : skillsStr.split(",")) {
                    String t = s.trim().toLowerCase();
                    if (!t.isBlank()) applicantPhrases.add(t);
                }
            }
        }
        if (applicantPhrases.isEmpty()) return 0.0;

        List<String> reqSkills = offer.getRequiredSkills();
        List<String> reqQuals  = offer.getRequiredQualifications();
        boolean hasSkills = reqSkills != null && !reqSkills.isEmpty();
        boolean hasQuals  = reqQuals  != null && !reqQuals.isEmpty();

        if (hasSkills || hasQuals) {
            double skillsScore = hasSkills ? listSimilarity(reqSkills, applicantPhrases) : 0.0;
            double qualsScore  = hasQuals  ? listSimilarity(reqQuals,  applicantPhrases) : 0.0;

            double result = (hasSkills && hasQuals)
                    ? (0.7 * skillsScore + 0.3 * qualsScore)
                    : (hasSkills ? skillsScore : qualsScore);

            return Math.round(result * 10.0) / 10.0;
        }

        StringBuilder jobText = new StringBuilder();
        if (offer.getTitle() != null) jobText.append(offer.getTitle()).append(" ");
        if (offer.getDescription() != null) jobText.append(offer.getDescription());

        Set<String> jobTokens = tokenize(jobText.toString());
        if (jobTokens.isEmpty()) return 0.0;

        Set<String> applicantTokens = new HashSet<>();
        for (String phrase : applicantPhrases) {
            applicantTokens.addAll(tokenize(phrase));
        }
        if (applicantTokens.isEmpty()) return 0.0;

        int matches = 0;
        for (String s : applicantTokens) if (jobTokens.contains(s)) matches++;

        double raw = (matches * 100.0) / applicantTokens.size();
        return Math.round(raw * 10.0) / 10.0;
    }


    // --- Status options for UI ---
    public ch.unil.doplab.ApplicationStatus[] getAllStatuses() {
        return ch.unil.doplab.ApplicationStatus.values();
    }

    public String labelFor(ch.unil.doplab.ApplicationStatus st) {
        if (st == null) return "";
        return switch (st) {
            case In_review -> "In Review";
            default -> st.name();
        };
    }

    // --- Getters/Setters ---

    public UUID getFilterJobOfferId() {
        return filterJobOfferId;
    }

    public void setFilterJobOfferId(UUID filterJobOfferId) {
        this.filterJobOfferId = filterJobOfferId;
    }

    public boolean isFilteredByJob() {
        return filterJobOfferId != null;
    }
}