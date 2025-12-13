package ch.unil.doplab.ui;

import ch.unil.doplab.JobOffer;
import ch.unil.doplab.client.JobFinderClient;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Named("jobReqBean")
@ViewScoped
public class EmployerRequirementBean implements Serializable {

    @Inject
    private JobFinderClient client;

    @Inject
    private EmployerJobFormBean formBean;

    // ESCO suggestions
    private String skillQuery;
    private List<Map<String, Object>> skillSuggestions = Collections.emptyList();

    private String qualificationQuery;
    private List<Map<String, Object>> qualificationSuggestions = Collections.emptyList();

    private JobOffer offer() {
        return formBean != null ? formBean.getJobOffer() : null;
    }

    // ============== Skills ==============

    public void refreshSkillSuggestions() {
        if (skillQuery == null || skillQuery.isBlank()) {
            skillSuggestions = Collections.emptyList();
            return;
        }
        skillSuggestions = client.suggestSkills(skillQuery, "skill", 10, "en");
    }

    public void addSkill(Map<String, Object> item) {
        JobOffer o = offer();
        if (o == null || item == null) return;

        Object raw = item.get("label");
        if (raw == null) return;

        String label = String.valueOf(raw).trim();
        if (label.isBlank()) return;

        if (o.getRequiredSkills() == null) {
            o.setRequiredSkills(new ArrayList<>());
        }
        if (!o.getRequiredSkills().contains(label)) {
            o.getRequiredSkills().add(label);
        }
    }

    public void removeSkill(String label) {
        JobOffer o = offer();
        if (o == null || label == null || o.getRequiredSkills() == null) return;

        o.getRequiredSkills().removeIf(s -> label.equalsIgnoreCase(s));
    }

    // ============== Qualifications ==============

    public void refreshQualificationSuggestions() {
        if (qualificationQuery == null || qualificationQuery.isBlank()) {
            qualificationSuggestions = Collections.emptyList();
            return;
        }
        qualificationSuggestions = client.suggestSkills(qualificationQuery, "qualification", 10, "en");
    }

    public void addQualification(Map<String, Object> item) {
        JobOffer o = offer();
        if (o == null || item == null) return;

        Object raw = item.get("label");
        if (raw == null) return;

        String label = String.valueOf(raw).trim();
        if (label.isBlank()) return;

        if (o.getRequiredQualifications() == null) {
            o.setRequiredQualifications(new ArrayList<>());
        }
        if (!o.getRequiredQualifications().contains(label)) {
            o.getRequiredQualifications().add(label);
        }
    }

    public void removeQualification(String label) {
        JobOffer o = offer();
        if (o == null || label == null || o.getRequiredQualifications() == null) return;

        o.getRequiredQualifications().removeIf(q -> label.equalsIgnoreCase(q));
    }

    // ============== Getters / Setters ==============

    public String getSkillQuery() { return skillQuery; }
    public void setSkillQuery(String skillQuery) { this.skillQuery = skillQuery; }
    public List<Map<String, Object>> getSkillSuggestions() { return skillSuggestions; }

    public String getQualificationQuery() { return qualificationQuery; }
    public void setQualificationQuery(String qualificationQuery) { this.qualificationQuery = qualificationQuery; }
    public List<Map<String, Object>> getQualificationSuggestions() { return qualificationSuggestions; }
}
