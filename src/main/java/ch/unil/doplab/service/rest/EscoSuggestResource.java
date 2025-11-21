package ch.unil.doplab.service.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import jakarta.json.bind.JsonbBuilder;

/**
 * REST endpoint for ESCO Skills / Occupations suggestions.
 */
@Path("/skills")
@Produces(MediaType.APPLICATION_JSON)
public class EscoSuggestResource {

    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Autocomplete ESCO skills or occupations.
     * Examples :
     *   /api/skills/suggest?q=data&type=skill&limit=5&lang=en
     *   /api/skills/suggest?q=java&type=occupation&limit=5&lang=en
     */
    @GET
    @Path("/suggest")
    public List<Map<String, Object>> suggest(
            @QueryParam("q") String q,
            @DefaultValue("skill") @QueryParam("type") String type,
            @DefaultValue("en") @QueryParam("lang") String lang,
            @DefaultValue("5") @QueryParam("limit") int limit
    ) {

        if (q == null || q.isBlank()) {
            throw new BadRequestException("Query parameter 'q' is required.");
        }

        type = type.equalsIgnoreCase("occupation") ? "occupation" : "skill";
        limit = Math.max(1, Math.min(25, limit)); // safe cap

        try {
            String url = String.format(
                    "https://ec.europa.eu/esco/api/search?text=%s&language=%s&type=%s&limit=%d&full=false",
                    URLEncoder.encode(q, StandardCharsets.UTF_8),
                    lang,
                    type,
                    limit
            );

            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "JobFinder/1.0")
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() >= 400) {
                throw new WebApplicationException(
                        "ESCO responded with HTTP " + res.statusCode(),
                        Response.Status.BAD_GATEWAY
                );
            }

            var jsonb = JsonbBuilder.create();
            Map<?, ?> parsed = jsonb.fromJson(res.body(), Map.class);

            Map<?, ?> embedded = (Map<?, ?>) parsed.get("_embedded");

            List<Map<String, Object>> results;

            if (embedded != null) {
                Object raw = embedded.get("results");
                if (raw instanceof List<?>) {
                    // Cast safe car nous itérons en Map<String,Object>
                    results = (List<Map<String, Object>>) raw;
                } else {
                    results = Collections.emptyList();
                }
            } else {
                results = Collections.emptyList();
            }

            List<Map<String, Object>> out = new ArrayList<>();

            for (Map<String, Object> r : results) {

                Map<String, Object> item = new LinkedHashMap<>();

                // Preferred label
                Map<String, Object> preferredLabel =
                        (Map<String, Object>) r.get("preferredLabel");

                String label = null;

                if (preferredLabel != null) {
                    Object l = preferredLabel.get(lang);
                    if (l != null) {
                        label = String.valueOf(l);
                    } else if (!preferredLabel.isEmpty()) {
                        label = String.valueOf(preferredLabel.values().iterator().next());
                    }
                }

                if (label == null) {
                    Object title = r.get("title");
                    label = title != null ? String.valueOf(title) : "";
                }

                item.put("label", label);

                String uri = String.valueOf(r.get("uri"));
                item.put("uri", uri);

                // Determine refined type
                String cls = String.valueOf(r.get("type"));
                if (cls == null || cls.isBlank() || "null".equalsIgnoreCase(cls)) {
                    if (uri != null && uri.contains("/skill/")) cls = "skill";
                    else if (uri != null && uri.contains("/occupation/")) cls = "occupation";
                    else cls = type; // fallback
                }
                item.put("type", cls);

                // Optional simplified ID
                if (uri != null) {
                    int slash = uri.lastIndexOf('/');
                    if (slash > 0 && slash < uri.length() - 1) {
                        item.put("id", uri.substring(slash + 1));
                    }
                }

                out.add(item);
            }

            return out;

        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException("ESCO search failed: " + e.getMessage());
        }
    }
}
