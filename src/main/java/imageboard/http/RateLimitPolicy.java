package imageboard.http;

import java.time.Duration;

public record RateLimitPolicy(int maxRequestsPerWindow, Duration window) {
    public static RateLimitPolicy defaultPolicy() {
        return new RateLimitPolicy(2, Duration.ofSeconds(1));
    }
}
