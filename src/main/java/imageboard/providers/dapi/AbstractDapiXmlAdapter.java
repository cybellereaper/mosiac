package imageboard.providers.dapi;

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

public abstract class AbstractDapiXmlAdapter implements ProviderAdapter {
    protected static final ProviderCapabilities DAPI_CAPABILITIES =
            new ProviderCapabilities(true, false, false, Set.of());

    private final String baseUrl;
    private final HttpExecutor executor;

    protected AbstractDapiXmlAdapter(String baseUrl, HttpExecutor executor) {
        this.baseUrl = baseUrl;
        this.executor = executor;
    }

    @Override
    public SearchResult<Post> searchPosts(SearchQuery query) {
        Document doc = call(UriBuilder.from(baseUrl, "/index.php")
                .query("page", "dapi")
                .query("s", "post")
                .query("q", "index")
                .query("tags", String.join(" ", query.tags()))
                .query("pid", effectivePage(query))
                .query("limit", query.limit()));
        List<Post> posts = mapPosts(doc);
        int page = effectivePage(query);
        int limit = query.limit() == null ? 100 : query.limit();
        return new SearchResult<>(posts, page, limit, posts.size() == limit);
    }

    @Override
    public Optional<Post> getPostById(long postId) {
        Document doc = call(UriBuilder.from(baseUrl, "/index.php")
                .query("page", "dapi")
                .query("s", "post")
                .query("q", "index")
                .query("id", postId));
        NodeList nodes = doc.getElementsByTagName("post");
        if (nodes.getLength() == 0) return Optional.empty();
        return Optional.of(mapPost((Element) nodes.item(0)));
    }

    @Override
    public SearchResult<Tag> searchTags(String name, int page, int limit) {
        Document doc = call(UriBuilder.from(baseUrl, "/index.php")
                .query("page", "dapi")
                .query("s", "tag")
                .query("q", "index")
                .query("name_pattern", name)
                .query("pid", page)
                .query("limit", limit));
        NodeList nodes = doc.getElementsByTagName("tag");
        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            tags.add(mapTag((Element) nodes.item(i)));
        }
        return new SearchResult<>(tags, page, limit, tags.size() == limit);
    }

    protected abstract String providerKey();

    protected Post mapPost(Element element) {
        String fileUrl = attr(element, "file_url");
        return new Post(
                longAttr(element, "id"),
                parseEpoch(attr(element, "created_at")),
                null,
                split(attr(element, "tags")),
                attr(element, "source"),
                Rating.fromProviderValue(attr(element, "rating")),
                intAttr(element, "score"),
                intAttr(element, "width"),
                intAttr(element, "height"),
                fileUrl,
                attr(element, "preview_url"),
                attr(element, "sample_url"),
                extension(fileUrl),
                mediaType(fileUrl),
                new ProviderMetadata(providerKey(), attributes(element), null)
        );
    }

    protected Tag mapTag(Element element) {
        return new Tag(
                longAttr(element, "id"),
                attr(element, "name"),
                intAttr(element, "count"),
                attr(element, "type"),
                new ProviderMetadata(providerKey(), attributes(element), null)
        );
    }

    private List<Post> mapPosts(Document doc) {
        NodeList nodes = doc.getElementsByTagName("post");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            posts.add(mapPost((Element) nodes.item(i)));
        }
        return posts;
    }

    private Document call(UriBuilder builder) {
        var response = executor.execute(providerKey(),
                new HttpRequestData(HttpMethod.GET, builder.build(), Map.of(), Duration.ofSeconds(20)));
        return XmlSupport.read(response.body());
    }

    private static int effectivePage(SearchQuery query) {
        if (query.pid() != null) return query.pid();
        return query.page() == null ? 0 : query.page();
    }

    private static List<String> split(String tags) {
        return tags == null || tags.isBlank() ? List.of() : Arrays.asList(tags.split("\\s+"));
    }

    private static String attr(Element e, String key) { return e.getAttribute(key); }
    private static Integer intAttr(Element e, String key) { String v = attr(e, key); return v.isBlank() ? null : Integer.parseInt(v); }
    private static long longAttr(Element e, String key) { String v = attr(e, key); return v.isBlank() ? 0L : Long.parseLong(v); }
    private static Instant parseEpoch(String epoch) { return epoch == null || epoch.isBlank() ? null : Instant.ofEpochSecond(Long.parseLong(epoch)); }

    private static Map<String, String> attributes(Element e) {
        Map<String, String> out = new LinkedHashMap<>();
        var attrs = e.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            out.put(attrs.item(i).getNodeName(), attrs.item(i).getNodeValue());
        }
        return out;
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
