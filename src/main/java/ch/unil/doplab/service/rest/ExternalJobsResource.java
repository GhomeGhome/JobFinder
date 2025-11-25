package ch.unil.doplab.service.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.*;
import jakarta.json.bind.JsonbBuilder;

/**
 * Fetch external jobs from RemoteOK public JSON API.
 */
@Path("/external")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalJobsResource {

    private static final HttpClient http = HttpClient.newHttpClient();

    /**
     * GET /api/external/remoteok?keyword=java&limit=5
     */
    @GET
    @Path("/remoteok")
    public List<Map<String,Object>> remoteok(
            @QueryParam("keyword") String keyword,
            @DefaultValue("5") @QueryParam("limit") int limit
    ) {
        try {
            HttpRequest req = HttpRequest.newBuilder(
                    URI.create("https://remoteok.com/api")
            )
            .header("User-Agent", "JobFinder/1.0")
            .GET()
            .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            var jsonb = JsonbBuilder.create();
            List<Map<String,Object>> arr = jsonb.fromJson(res.body(), List.class);

            if (!arr.isEmpty() && arr.get(0).containsKey("legal")) {
                arr = arr.subList(1, arr.size());
            }

            String q = (keyword == null ? "" : keyword.trim().toLowerCase());
            limit = Math.max(1, Math.min(limit, 50)); // safe cap
            List<Map<String,Object>> out = new ArrayList<>();

            for (var it : arr) {
                String position = String.valueOf(it.getOrDefault("position", ""));
                String company  = String.valueOf(it.getOrDefault("company", ""));
                String url      = String.valueOf(it.getOrDefault("url", ""));
                List<String> tags = (List<String>) it.getOrDefault("tags", List.of());

                boolean match = q.isBlank()
                        || position.toLowerCase().contains(q)
                        || company.toLowerCase().contains(q)
                        || tags.stream().anyMatch(t -> t.toLowerCase().contains(q));

                if (match) {
                    out.add(Map.of(
                            "title", position,
                            "company", company,
                            "url", url,
                            "tags", tags
                    ));

                    if (out.size() >= limit) break;
                }
            }

            return out;

        } catch (Exception e) {
            throw new InternalServerErrorException("RemoteOK fetch failed: " + e.getMessage());
        }
    }
}
