package ch.unil.doplab.service.rest;

import ch.unil.doplab.Interview;
import ch.unil.doplab.InterviewStatus;
import ch.unil.doplab.service.domain.ApplicationState;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Path("/interviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InterviewResource {

    @Inject
    private ApplicationState state;

    @GET
    public List<Interview> all() {
        return state.listInterviews();
    }

    @GET
    @Path("/by-employer/{employerId}")
    public List<Interview> byEmployer(@PathParam("employerId") String employerIdStr) {
        UUID employerId = UUID.fromString(employerIdStr);
        return state.listInterviewsByEmployerId(employerId);
    }

    @GET
    @Path("/by-applicant/{applicantId}")
    public List<Interview> byApplicant(@PathParam("applicantId") String applicantIdStr) {
        UUID applicantId = UUID.fromString(applicantIdStr);
        return state.listInterviewsByApplicantId(applicantId);
    }

    public static class CreateInterviewRequest {
        public String jobOfferId;
        public String applicantId;
        public Long scheduledAtMillis;
        public String mode;
        public String locationOrLink;
    }

    @POST
    public Interview create(CreateInterviewRequest req) {
        if (req == null)
            throw new BadRequestException("Body is required");
        if (req.jobOfferId == null || req.jobOfferId.isBlank())
            throw new BadRequestException("jobOfferId is required");
        if (req.applicantId == null || req.applicantId.isBlank())
            throw new BadRequestException("applicantId is required");
        if (req.scheduledAtMillis == null)
            throw new BadRequestException("scheduledAtMillis is required");

        UUID jobOfferId = UUID.fromString(req.jobOfferId);
        UUID applicantId = UUID.fromString(req.applicantId);
        Date scheduledAt = new Date(req.scheduledAtMillis);

        return state.createInterview(jobOfferId, applicantId, scheduledAt, req.mode, req.locationOrLink);
    }

    @GET
    @Path("/{id}")
    public Interview getById(@PathParam("id") String idStr) {
        UUID id = UUID.fromString(idStr);
        Interview iv = state.getInterview(id);
        if (iv == null)
            throw new NotFoundException("Interview not found");
        return iv;
    }

    @POST
    @Path("/{id}/status/{status}")
    public Interview updateStatus(@PathParam("id") String idStr,
            @PathParam("status") String statusRaw) {

        UUID id = UUID.fromString(idStr);
        InterviewStatus status;
        try {
            status = InterviewStatus.valueOf(statusRaw);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid InterviewStatus: " + statusRaw);
        }

        Interview updated = state.updateInterviewStatus(id, status);
        if (updated == null)
            throw new NotFoundException("Interview not found");
        return updated;
    }

    public static class RescheduleRequest {
        public Long scheduledAtMillis;
        public String mode;
    }

    @POST
    @Path("/{id}/reschedule")
    public Interview reschedule(@PathParam("id") String idStr, RescheduleRequest req) {
        UUID id = UUID.fromString(idStr);
        if (req == null || req.scheduledAtMillis == null)
            throw new BadRequestException("scheduledAtMillis is required");

        Date newDate = new Date(req.scheduledAtMillis);
        Interview updated = state.rescheduleInterview(id, newDate, req.mode);
        if (updated == null)
            throw new NotFoundException("Interview not found");
        return updated;
    }

    public static class UpdateDetailsRequest {
        public String locationOrLink;
    }

    @POST
    @Path("/{id}/details")
    public Interview updateDetails(@PathParam("id") String idStr, UpdateDetailsRequest req) {
        UUID id = UUID.fromString(idStr);
        if (req == null || req.locationOrLink == null)
            throw new BadRequestException("locationOrLink is required");

        Interview updated = state.updateInterviewDetails(id, req.locationOrLink);
        if (updated == null)
            throw new NotFoundException("Interview not found");
        return updated;
    }
}
