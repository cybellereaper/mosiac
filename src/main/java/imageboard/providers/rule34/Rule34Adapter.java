package imageboard.providers.rule34;

import imageboard.auth.AuthConfig;
import imageboard.http.HttpMethod;
import imageboard.http.HttpRequestData;
import imageboard.internal.HttpExecutor;
import imageboard.internal.UriBuilder;
import imageboard.model.*;
import imageboard.providers.ProviderAdapter;
import imageboard.query.SearchQuery;
import imageboard.serialization.XmlSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class Rule34Adapter implements ProviderAdapter {
    private static final ProviderCapabilities CAPABILITIES =
            new ProviderCapabilities(true, false, false, Set.of());

    private final String baseUrl;
    private final HttpExecutor executor;

    public Rule34Adapter(String baseUrl, HttpExecutor executor, AuthConfig auth) {
        this.baseUrl = baseUrl;
        this.executor = executor;
    }

    @Override public String providerId() { return "rule34"; }
    @Override public ProviderCapabilities capabilities() { return CAPABILITIES; }

    @Override
    public SearchResult<Post> searchPosts(SearchQuery query) {
        Document doc = call(UriBuilder.from(baseUrl, "/index.php")
                .query("page", "dapi").query("s", "post").query("q", "index")
                .query("tags", String.join(" ", query.tags()))
                .query("pid", query.pid() == null ? query.page() : query.pid())
                .query("limit", query.limit()));
        NodeList nodes = doc.getElementsByTagName("post");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) posts.add(mapPost((Element) nodes.item(i)));
        int page = query.pid() == null ? (query.page() == null ? 0 : query.page()) : query.pid();
        int limit = query.limit() == null ? 100 : query.limit();
        return new SearchResult<>(posts, page, limit, posts.size() == limit);
    }

    @Override
    public Optional<Post> getPostById(long postId) {
        Document doc = call(UriBuilder.from(baseUrl, "/index.php")
                .query("page", "dapi").query("s", "post").query("q", "index")
                .query("id", postId));
        NodeList nodes = doc.getElementsByTagName("post");
        if (nodes.getLength() == 0) return Optional.empty();
        return Optional.of(mapPost((Element) nodes.item(0)));
    }

    @Override
    public SearchResult<Tag> searchTags(String name, int page, int limit) {
        Document doc = call(UriBuilder.from(baseUrl, "/index.php")
                .query("page", "dapi").query("s", "tag").query("q", "index")
                .query("name_pattern", name)
                .query("pid", page)
                .query("limit", limit));
        NodeList nodes = doc.getElementsByTagName("tag");
        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) tags.add(mapTag((Element) nodes.item(i)));
        return new SearchResult<>(tags, page, limit, tags.size() == limit);
    }

    private Document call(UriBuilder builder) {
        var response = executor.execute(providerId(), new HttpRequestData(HttpMethod.GET, builder.build(), Map.of(), Duration.ofSeconds(20)));
        return XmlSupport.read(response.body());
    }

    private Post mapPost(Element e) {
        String fileUrl = e.getAttribute("file_url");
        return new Post(
                longAttr(e, "id"),
                parseEpoch(e.getAttribute("created_at")),
                null,
                split(e.getAttribute("tags")),
                e.getAttribute("source"),
                Rating.fromProviderValue(e.getAttribute("rating")),
                intAttr(e, "score"),
                intAttr(e, "width"),
                intAttr(e, "height"),
                fileUrl,
                e.getAttribute("preview_url"),
                e.getAttribute("sample_url"),
                extension(fileUrl),
                mediaType(fileUrl),
                new ProviderMetadata(providerId(), attrs(e), null)
        );
    }

    private Tag mapTag(Element e) {
        return new Tag(longAttr(e, "id"), e.getAttribute("name"), intAttr(e, "count"), e.getAttribute("type"),
                new ProviderMetadata(providerId(), attrs(e), null));
    }

    private static long longAttr(Element e, String name) { String v = e.getAttribute(name); return v.isBlank() ? 0 : Long.parseLong(v); }
    private static Integer intAttr(Element e, String name) { String v = e.getAttribute(name); return v.isBlank() ? null : Integer.parseInt(v); }
    private static Instant parseEpoch(String value) { return value == null || value.isBlank() ? null : Instant.ofEpochSecond(Long.parseLong(value)); }
    private static List<String> split(String tags) { return tags == null || tags.isBlank() ? List.of() : Arrays.asList(tags.split("\\s+")); }

    private static Map<String, String> attrs(Element e) {
        Map<String, String> map = new LinkedHashMap<>();
        var attrs = e.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) map.put(attrs.item(i).getNodeName(), attrs.item(i).getNodeValue());
        return map;
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
}
