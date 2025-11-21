package ch.unil.doplab;

/**
 * Statuts possibles d'une offre d'emploi.
 * Cycle complet :
 *   Draft → Published → Closed → Reopened
 */
public enum JobOfferStatus {
    Draft,
    Published,
    Closed,
    Reopened
}
