package ch.unil.doplab.service.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * This class activates the REST API at the URL: http://localhost:8080/jobfinder/api
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
    // No code needed here. The annotation does all the work.
}