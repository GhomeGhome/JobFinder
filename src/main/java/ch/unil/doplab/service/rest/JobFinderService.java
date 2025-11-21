package ch.unil.doplab.service.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Racine de l'API REST.
 * Toutes les ressources accessibles sous: /api/...
 */
@ApplicationPath("/api")
public class JobFinderService extends Application {
    // No additional configuration needed.
}
