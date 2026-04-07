package imageboard.providers.gelbooru;

import imageboard.auth.AuthConfig;
import imageboard.errors.UnsupportedOperationException;
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

public final class GelbooruAdapter implements ProviderAdapter {
    private static final ProviderCapabilities CAPABILITIES =
            new ProviderCapabilities(true, false, false, Set.of());

    private final String baseUrl;
    private final HttpExecutor executor;

    public GelbooruAdapter(String baseUrl, HttpExecutor executor, AuthConfig auth) {
        this.baseUrl = baseUrl;
        this.executor = executor;
    }

    @Override public String providerId() { return "gelbooru"; }
    @Override public ProviderCapabilities capabilities() { return CAPABILITIES; }

    @Override
    public SearchResult<Post> searchPosts(SearchQuery query) {
        UriBuilder uri = UriBuilder.from(baseUrl, "/index.php")
                .query("page", "dapi").query("s", "post").query("q", "index")
                .query("tags", String.join(" ", query.tags()))
                .query("pid", query.pid() == null ? query.page() : query.pid())
                .query("limit", query.limit());
        Document doc = call(uri);
        NodeList nodes = doc.getElementsByTagName("post");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) posts.add(mapPost((Element) nodes.item(i)));
        int page = query.pid() == null ? (query.page() == null ? 0 : query.page()) : query.pid();
        int limit = query.limit() == null ? 100 : query.limit();
        return new SearchResult<>(posts, page, limit, posts.size() == limit);
    }

    @Override
    public Optional<Post> getPostById(long postId) {
        UriBuilder uri = UriBuilder.from(baseUrl, "/index.php")
                .query("page", "dapi").query("s", "post").query("q", "index")
                .query("id", postId);
        Document doc = call(uri);
        NodeList nodes = doc.getElementsByTagName("post");
        if (nodes.getLength() == 0) return Optional.empty();
        return Optional.of(mapPost((Element) nodes.item(0)));
    }

    @Override
    public SearchResult<Tag> searchTags(String name, int page, int limit) {
        UriBuilder uri = UriBuilder.from(baseUrl, "/index.php")
                .query("page", "dapi").query("s", "tag").query("q", "index")
                .query("name_pattern", name)
                .query("pid", page)
                .query("limit", limit);
        Document doc = call(uri);
        NodeList nodes = doc.getElementsByTagName("tag");
        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) tags.add(mapTag((Element) nodes.item(i)));
        return new SearchResult<>(tags, page, limit, tags.size() == limit);
    }

    private Document call(UriBuilder uriBuilder) {
        var response = executor.execute(providerId(), new HttpRequestData(HttpMethod.GET, uriBuilder.build(), Map.of(), Duration.ofSeconds(20)));
        return XmlSupport.read(response.body());
    }

    private Post mapPost(Element e) {
        String fileUrl = attr(e, "file_url");
        return new Post(
                longAttr(e, "id"),
                parseEpoch(attr(e, "created_at")),
                null,
                split(attr(e, "tags")),
                attr(e, "source"),
                Rating.fromProviderValue(attr(e, "rating")),
                intAttr(e, "score"),
                intAttr(e, "width"),
                intAttr(e, "height"),
                fileUrl,
                attr(e, "preview_url"),
                attr(e, "sample_url"),
                extension(fileUrl),
                mediaType(fileUrl),
                new ProviderMetadata(providerId(), attrs(e), null)
        );
    }

    private Tag mapTag(Element e) {
        return new Tag(longAttr(e, "id"), attr(e, "name"), intAttr(e, "count"), attr(e, "type"),
                new ProviderMetadata(providerId(), attrs(e), null));
    }

    private static List<String> split(String tags) { return tags == null || tags.isBlank() ? List.of() : Arrays.asList(tags.split("\\s+")); }
    private static String attr(Element e, String name) { return e.getAttribute(name); }
    private static Integer intAttr(Element e, String name) { var v = attr(e, name); return v.isBlank() ? null : Integer.parseInt(v); }
    private static long longAttr(Element e, String name) { var v = attr(e, name); return v.isBlank() ? 0L : Long.parseLong(v); }
    private static Instant parseEpoch(String epoch) { return epoch == null || epoch.isBlank() ? null : Instant.ofEpochSecond(Long.parseLong(epoch)); }

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
