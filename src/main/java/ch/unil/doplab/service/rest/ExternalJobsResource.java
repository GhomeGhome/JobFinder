package ch.unil.doplab.service.rest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import jakarta.json.bind.JsonbBuilder;

@Path("/external")
@Produces(MediaType.APPLICATION_JSON)
public class ExternalJobsResource {
    private static final HttpClient http = HttpClient.newHttpClient();

    @GET @Path("/remoteok")
    public List<Map<String,Object>> remoteok(@QueryParam("keyword") String keyword,
                                             @DefaultValue("5") @QueryParam("limit") int limit) {
        try {
            // RemoteOK public JSON (API/JSON feed)
            var req = HttpRequest.newBuilder(URI.create("https://remoteok.com/api"))
                    .header("User-Agent","JobFinder/1.0 (igor@example.com)")
                    .GET().build();
            var res = http.send(req, HttpResponse.BodyHandlers.ofString());

            var jsonb = JsonbBuilder.create();
            List<Map<String,Object>> arr = jsonb.fromJson(res.body(), List.class);

            // API returns an info object as first element -> drop it
            if (!arr.isEmpty() && arr.get(0).containsKey("legal")) {
                arr = arr.subList(1, arr.size());
            }

            String q = (keyword == null ? "" : keyword).toLowerCase();
            List<Map<String,Object>> out = new ArrayList<>();
            for (var it : arr) {
                String position = String.valueOf(it.getOrDefault("position",""));
                String company  = String.valueOf(it.getOrDefault("company",""));
                String url      = String.valueOf(it.getOrDefault("url",""));
                List<String> tags = (List<String>) it.getOrDefault("tags", List.of());

                boolean match = q.isBlank()
                        || position.toLowerCase().contains(q)
                        || company.toLowerCase().contains(q)
                        || tags.stream().anyMatch(t -> t.toLowerCase().contains(q));

                if (match) {
                    out.add(Map.of(
                            "title", position,
                            "company", company,
                            "url", url,        // <- link back (attribution)
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
