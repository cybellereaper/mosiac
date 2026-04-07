package imageboard.internal;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class UriBuilder {
    private final String base;
    private final String path;
    private final Map<String, String> query = new LinkedHashMap<>();

    private UriBuilder(String base, String path) {
        this.base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        this.path = path.startsWith("/") ? path : "/" + path;
    }

    public static UriBuilder from(String base, String path) {
        return new UriBuilder(base, path);
    }

    public UriBuilder query(String key, Object value) {
        if (value != null) query.put(key, String.valueOf(value));
        return this;
    }

    public URI build() {
        if (query.isEmpty()) return URI.create(base + path);
        StringJoiner joiner = new StringJoiner("&");
        query.forEach((k, v) -> joiner.add(enc(k) + "=" + enc(v)));
        return URI.create(base + path + "?" + joiner);
    }

    private static String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }
}
