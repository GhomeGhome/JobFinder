package ch.unil.doplab.ui;

import ch.unil.doplab.JobOffer;
import ch.unil.doplab.client.JobFinderClient;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Named("jobReqBean")
@SessionScoped
public class EmployerRequirementBean implements Serializable {

    @Inject
    private JobFinderClient client;

    @Inject
    private EmployerJobFormBean formBean;

    // ESCO suggestions
    private String skillQuery;
    private List<Map<String, Object>> skillSuggestions = Collections.emptyList();
    private String selectedSkillLabel;  // For f:setPropertyActionListener

    private String qualificationQuery;
    private List<Map<String, Object>> qualificationSuggestions = Collections.emptyList();
    private String selectedQualificationLabel;  // For f:setPropertyActionListener

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
    
    // Add skill by index (for AJAX compatibility)
    public void addSkillByIndex(int index) {
        System.out.println("[DEBUG] addSkillByIndex called with index: " + index);
        System.out.println("[DEBUG] skillSuggestions size: " + skillSuggestions.size());
        System.out.println("[DEBUG] formBean: " + formBean);
        System.out.println("[DEBUG] offer: " + offer());
        
        if (index < 0 || index >= skillSuggestions.size()) {
            System.out.println("[DEBUG] Index out of bounds, returning");
            return;
        }
        Map<String, Object> item = skillSuggestions.get(index);
        System.out.println("[DEBUG] Item to add: " + item);
        addSkill(item);
        
        JobOffer o = offer();
        if (o != null) {
            System.out.println("[DEBUG] After add, requiredSkills: " + o.getRequiredSkills());
        }
    }
    
    // Add skill by label directly (alternative approach)
    public void addSkillByLabel(String label) {
        System.out.println("[DEBUG] addSkillByLabel called with: " + label);
        JobOffer o = offer();
        if (o == null || label == null || label.isBlank()) {
            System.out.println("[DEBUG] Offer null or label blank");
            return;
        }
        
        if (o.getRequiredSkills() == null) {
            o.setRequiredSkills(new java.util.ArrayList<>());
        }
        if (!o.getRequiredSkills().contains(label)) {
            o.getRequiredSkills().add(label);
            System.out.println("[DEBUG] Skill added: " + label);
        }
    }
    
    // Action method for f:setPropertyActionListener approach
    public void addSelectedSkill() {
        System.out.println("[DEBUG] addSelectedSkill called, selectedSkillLabel: " + selectedSkillLabel);
        if (selectedSkillLabel != null && !selectedSkillLabel.isBlank()) {
            addSkillByLabel(selectedSkillLabel);
            selectedSkillLabel = null; // reset
        }
    }
    
    // ActionListener version (for immediate=true)
    public void addSelectedSkill(jakarta.faces.event.ActionEvent event) {
        System.out.println("[DEBUG] addSelectedSkill(ActionEvent) called, selectedSkillLabel: " + selectedSkillLabel);
        addSelectedSkill();
    }
    
    public String getSelectedSkillLabel() { return selectedSkillLabel; }
    public void setSelectedSkillLabel(String label) { this.selectedSkillLabel = label; }

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

    // Add qualification by index (for AJAX compatibility)
    public void addQualificationByIndex(int index) {
        if (index < 0 || index >= qualificationSuggestions.size()) return;
        addQualification(qualificationSuggestions.get(index));
    }
    
    // Add qualification by label directly
    public void addQualificationByLabel(String label) {
        JobOffer o = offer();
        if (o == null || label == null || label.isBlank()) return;
        
        if (o.getRequiredQualifications() == null) {
            o.setRequiredQualifications(new java.util.ArrayList<>());
        }
        if (!o.getRequiredQualifications().contains(label)) {
            o.getRequiredQualifications().add(label);
        }
    }
    
    // Action method for f:setPropertyActionListener approach
    public void addSelectedQualification() {
        if (selectedQualificationLabel != null && !selectedQualificationLabel.isBlank()) {
            addQualificationByLabel(selectedQualificationLabel);
            selectedQualificationLabel = null; // reset
        }
    }
    
    // ActionListener version (for immediate=true)
    public void addSelectedQualification(jakarta.faces.event.ActionEvent event) {
        addSelectedQualification();
    }
    
    public String getSelectedQualificationLabel() { return selectedQualificationLabel; }
    public void setSelectedQualificationLabel(String label) { this.selectedQualificationLabel = label; }

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
