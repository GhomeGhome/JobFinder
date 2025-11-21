package ch.unil.doplab;

/**
 * Statuts possibles d'une candidature dans le système.
 * Cycle complet :
 *   Submitted → In_review → Rejected / Accepted / Withdrawn
 */
public enum ApplicationStatus {
    Submitted,
    In_review,
    Rejected,
    Accepted,
    Withdrawn
}
