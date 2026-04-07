package imageboard.http;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

public record HttpRequestData(HttpMethod method, URI uri, Map<String, String> headers, Duration readTimeout) {
}
