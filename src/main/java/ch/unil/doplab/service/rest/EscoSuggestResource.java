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

@Path("/skills")
@Produces(MediaType.APPLICATION_JSON)
public class EscoSuggestResource {

    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Autocomplete ESCO skills or occupations.
     * Examples:
     *  /api/skills/suggest?q=data&type=skill&limit=5&lang=en
     *  /api/skills/suggest?q=java&type=occupation&limit=5&lang=en
     */
    @GET
    @Path("/suggest")
    public List<Map<String, Object>> suggest(
            @QueryParam("q") String q,
            @DefaultValue("skill") @QueryParam("type") String type,         // "skill" or "occupation"
            @DefaultValue("en") @QueryParam("lang") String lang,            // "en", "fr", "de", ...
            @DefaultValue("5") @QueryParam("limit") int limit
    ) {
        if (q == null || q.isBlank()) {
            throw new BadRequestException("Query parameter 'q' is required");
        }
        type = type.equalsIgnoreCase("occupation") ? "occupation" : "skill";
        limit = Math.max(1, Math.min(25, limit)); // cap to be nice

        try {
            String url = String.format(
                    "https://ec.europa.eu/esco/api/search?text=%s&language=%s&type=%s&limit=%d&full=false",
                    URLEncoder.encode(q, StandardCharsets.UTF_8), lang, type, limit
            );

            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .header("User-Agent", "JobFinder/1.0 (contact@example.com)")
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
            Map<?, ?> root = jsonb.fromJson(res.body(), Map.class);

            // ESCO returns items under _embedded.results (HAL-ish)
            Map<?, ?> embedded = (Map<?, ?>) root.get("_embedded");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = Collections.emptyList();
            if (embedded != null) {
                Object val = embedded.get("results");
                if (val instanceof List) {
                    results = (List<Map<String, Object>>) val;
                }
            }

            // Normalize into a small, clean list
            List<Map<String, Object>> out = new ArrayList<>();
            for (Map<String, Object> r : results) {
                @SuppressWarnings("unchecked")
                Map<String, Object> preferredLabel = (Map<String, Object>) r.get("preferredLabel");

                String label = null;
                if (preferredLabel != null) {
                    Object l = preferredLabel.get(lang);
                    if (l != null) {
                        label = String.valueOf(l);
                    } else if (!preferredLabel.isEmpty()) {
                        Object first = preferredLabel.values().iterator().next();
                        label = first != null ? String.valueOf(first) : null;
                    }
                }
                if (label == null) {
                    Object t = r.get("title");
                    label = t != null ? String.valueOf(t) : "";
                }

                String uri = String.valueOf(r.get("uri"));

                // Derive type BEFORE adding to the output
                String cls = r.get("type") != null ? String.valueOf(r.get("type")) : null;
                if (cls == null || "null".equalsIgnoreCase(cls) || cls.isBlank()) {
                    if (uri != null) {
                        if (uri.contains("/skill/")) cls = "skill";
                        else if (uri.contains("/occupation/")) cls = "occupation";
                    }
                    if (cls == null || cls.isBlank()) {
                        cls = type; // fallback to requested type (skill/occupation)
                    }
                }

                // Optional: compact ID from URI’s last segment
                String id = null;
                if (uri != null) {
                    int i = uri.lastIndexOf('/');
                    if (i > 0 && i < uri.length() - 1) id = uri.substring(i + 1);
                }

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("label", label);
                item.put("uri", uri);
                item.put("type", cls);
                if (id != null) item.put("id", id);
                out.add(item);
            }
            return out;

        } catch (WebApplicationException waex) {
            throw waex;
        } catch (Exception e) {
            throw new InternalServerErrorException("ESCO search failed: " + e.getMessage());
        }
    }
}
