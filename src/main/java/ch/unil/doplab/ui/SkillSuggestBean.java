package ch.unil.doplab.ui;

import ch.unil.doplab.Applicant;
import ch.unil.doplab.client.JobFinderClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.*;

@Named("skillBean")
@SessionScoped
public class SkillSuggestBean implements Serializable {

    @Inject
    private JobFinderClient client;

    @Inject
    private LoginBean loginBean;

    private String query;
    private List<Map<String, Object>> suggestions = Collections.emptyList();

    // UI chips
    private List<Map<String, Object>> selectedSkills = new ArrayList<>();

    @PostConstruct
    public void init() {
        syncFromApplicant();
    }

    public void refreshSuggestions() {
        if (query == null || query.isBlank()) {
            suggestions = Collections.emptyList();
            return;
        }
        suggestions = client.suggestSkills(query, "skill", 10, "en");
    }

    public void addSkill(Map<String, Object> skill) {
        if (skill == null) return;

        Object rawLabel = skill.get("label");
        if (rawLabel == null) return;

        String label = rawLabel.toString().trim();
        if (label.isBlank()) return;

        // 1) Update UI list
        boolean already = selectedSkills.stream()
                .anyMatch(s -> label.equalsIgnoreCase(String.valueOf(s.get("label"))));
        if (!already) selectedSkills.add(skill);

        // 2) Persist into Applicant.skillsAsString
        persistSkillsToApplicantAndRecompute();
    }

    public void removeSkill(Map<String, Object> skill) {
        if (skill == null) return;

        Object rawLabel = skill.get("label");
        if (rawLabel == null) return;

        String label = rawLabel.toString().trim();
        if (label.isBlank()) return;

        selectedSkills.removeIf(s -> label.equalsIgnoreCase(String.valueOf(s.get("label"))));

        persistSkillsToApplicantAndRecompute();
    }

    private void persistSkillsToApplicantAndRecompute() {
        Applicant a = loginBean.getLoggedApplicant();
        if (a == null || a.getId() == null) return;

        // keep only unique labels
        LinkedHashSet<String> labels = new LinkedHashSet<>();
        for (Map<String, Object> s : selectedSkills) {
            Object l = s.get("label");
            if (l != null) {
                String val = String.valueOf(l).trim();
                if (!val.isBlank()) labels.add(val);
            }
        }

        a.setSkillsAsString(String.join(", ", labels));

        // IMPORTANT: persist applicant first
        boolean ok = client.updateApplicant(a);
        if (ok) {
            client.recomputeMatchScoresForApplicant(a.getId());
        }
    }

    // Optional: when opening profile page, load selectedSkills from skillsAsString
    public void syncFromApplicant() {
        Applicant a = loginBean.getLoggedApplicant();
        if (a == null) return;

        selectedSkills.clear();

        String skills = a.getSkillsAsString();
        if (skills == null || skills.isBlank()) return;

        for (String x : skills.split(",")) {
            String label = x.trim();
            if (!label.isBlank()) {
                selectedSkills.add(new LinkedHashMap<>(Map.of("label", label)));
            }
        }
    }

    // getters / setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public List<Map<String, Object>> getSuggestions() { return suggestions; }
    public List<Map<String, Object>> getSelectedSkills() { return selectedSkills; }
}
