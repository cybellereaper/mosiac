package imageboard.providers.danbooru;

import com.fasterxml.jackson.core.type.TypeReference;
import imageboard.auth.AuthConfig;
import imageboard.errors.UnsupportedOperationException;
import imageboard.http.HttpMethod;
import imageboard.http.HttpRequestData;
import imageboard.internal.HttpExecutor;
import imageboard.internal.UriBuilder;
import imageboard.model.*;
import imageboard.providers.ProviderAdapter;
import imageboard.query.SearchQuery;
import imageboard.serialization.JsonSupport;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class DanbooruAdapter implements ProviderAdapter {
    private static final ProviderCapabilities CAPABILITIES =
            new ProviderCapabilities(true, true, true, Set.of("id", "score", "updated_at"));

    private final String baseUrl;
    private final HttpExecutor executor;
    private final AuthConfig authConfig;
    private final Map<String, String> headers;

    public DanbooruAdapter(String baseUrl, HttpExecutor executor, AuthConfig authConfig, Map<String, String> headers) {
        this.baseUrl = baseUrl;
        this.executor = executor;
        this.authConfig = authConfig;
        this.headers = headers;
    }

    @Override public String providerId() { return "danbooru"; }
    @Override public ProviderCapabilities capabilities() { return CAPABILITIES; }

    @Override
    public SearchResult<Post> searchPosts(SearchQuery query) {
        URIBuilderParts parts = postUri(query, null);
        var response = executor.execute(providerId(), new HttpRequestData(HttpMethod.GET, parts.uri(), parts.headers(), Duration.ofSeconds(20)));
        List<Map<String, Object>> raw = JsonSupport.read(response.body(), new TypeReference<>() {});
        List<Post> posts = raw.stream().map(this::mapPost).toList();
        int page = query.page() == null ? 1 : query.page();
        int limit = query.limit() == null ? 20 : query.limit();
        return new SearchResult<>(posts, page, limit, posts.size() == limit);
    }

    @Override
    public Optional<Post> getPostById(long postId) {
        URIBuilderParts parts = postUri(SearchQuery.builder().build(), postId);
        var response = executor.execute(providerId(), new HttpRequestData(HttpMethod.GET, parts.uri(), parts.headers(), Duration.ofSeconds(20)));
        Map<String, Object> raw = JsonSupport.read(response.body(), new TypeReference<>() {});
        return Optional.of(mapPost(raw));
    }

    @Override
    public SearchResult<Tag> searchTags(String name, int page, int limit) {
        URIBuilderParts parts = auth(UriBuilder.from(baseUrl, "/tags.json")
                .query("search[name_matches]", name)
                .query("page", page)
                .query("limit", limit));
        var response = executor.execute(providerId(), new HttpRequestData(HttpMethod.GET, parts.uri(), parts.headers(), Duration.ofSeconds(20)));
        List<Map<String, Object>> raw = JsonSupport.read(response.body(), new TypeReference<>() {});
        List<Tag> tags = raw.stream().map(this::mapTag).toList();
        return new SearchResult<>(tags, page, limit, tags.size() == limit);
    }

    private URIBuilderParts postUri(SearchQuery query, Long id) {
        UriBuilder builder = id == null
                ? UriBuilder.from(baseUrl, "/posts.json")
                : UriBuilder.from(baseUrl, "/posts/" + id + ".json");
        if (id == null) {
            builder.query("tags", String.join(" ", query.tags()))
                    .query("page", query.page())
                    .query("limit", query.limit());
            if (query.sort() != null) builder.query("tags", "order:" + query.sort() + " " + String.join(" ", query.tags()));
        }
        return auth(builder);
    }

    private URIBuilderParts auth(UriBuilder builder) {
        if (authConfig instanceof AuthConfig.ApiKey key) {
            builder.query("login", key.username()).query("api_key", key.apiKey());
        }
        return new URIBuilderParts(builder.build(), headers);
    }

    private Post mapPost(Map<String, Object> raw) {
        List<String> tags = split(raw.get("tag_string"));
        String fileUrl = asString(raw.get("file_url"));
        return new Post(
                asLong(raw.get("id")),
                parseInstant(raw.get("created_at")),
                parseInstant(raw.get("updated_at")),
                tags,
                asString(raw.get("source")),
                Rating.fromProviderValue(asString(raw.get("rating"))),
                asInt(raw.get("score")),
                asInt(raw.get("image_width")),
                asInt(raw.get("image_height")),
                fileUrl,
                asString(raw.get("preview_file_url")),
                asString(raw.get("large_file_url")),
                extension(fileUrl),
                mediaType(fileUrl),
                new ProviderMetadata(providerId(), flatten(raw), null)
        );
    }

    private Tag mapTag(Map<String, Object> raw) {
        return new Tag(asLong(raw.get("id")), asString(raw.get("name")), asInt(raw.get("post_count")),
                asString(raw.get("category")), new ProviderMetadata(providerId(), flatten(raw), null));
    }

    private static Map<String, String> flatten(Map<String, Object> raw) {
        Map<String, String> m = new LinkedHashMap<>();
        raw.forEach((k, v) -> m.put(k, v == null ? "" : String.valueOf(v)));
        return m;
    }

    private static List<String> split(Object tags) {
        String value = asString(tags);
        return value == null || value.isBlank() ? List.of() : Arrays.asList(value.split("\\s+"));
    }

    private static Integer asInt(Object value) { return value == null ? null : Integer.parseInt(String.valueOf(value)); }
    private static long asLong(Object value) { return value == null ? 0 : Long.parseLong(String.valueOf(value)); }
    private static String asString(Object value) { return value == null ? null : String.valueOf(value); }
    private static Instant parseInstant(Object value) {
        String s = asString(value);
        if (s == null || s.isBlank()) return null;
        try { return Instant.parse(s); } catch (Exception ignored) { return null; }
    }
    private static String extension(String url) {
        if (url == null || !url.contains(".")) return null;
        return url.substring(url.lastIndexOf('.') + 1).toLowerCase();
    }
    private static String mediaType(String url) {
        String ext = extension(url);
        if (ext == null) return null;
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webm" -> "video/webm";
            default -> null;
        };
    }

    private record URIBuilderParts(java.net.URI uri, Map<String, String> headers) {}
}
