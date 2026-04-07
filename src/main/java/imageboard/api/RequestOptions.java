package imageboard.api;

import imageboard.http.RateLimitPolicy;
import imageboard.http.RetryPolicy;

import java.time.Duration;
import java.util.Map;

public record RequestOptions(
        Duration connectTimeout,
        Duration readTimeout,
        String userAgent,
        Map<String, String> defaultHeaders,
        RetryPolicy retryPolicy,
        RateLimitPolicy rateLimitPolicy
) {
    public RequestOptions {
        defaultHeaders = defaultHeaders == null ? Map.of() : Map.copyOf(defaultHeaders);
    }

    public static RequestOptions defaults() {
        return new RequestOptions(Duration.ofSeconds(10), Duration.ofSeconds(20),
                "imageboard-api/1.0", Map.of(), RetryPolicy.defaultPolicy(), RateLimitPolicy.defaultPolicy());
    }
}
