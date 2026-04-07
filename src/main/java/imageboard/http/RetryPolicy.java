package imageboard.http;

import java.time.Duration;

public record RetryPolicy(int maxAttempts, Duration backoff, boolean retryOnRateLimit, boolean retryOn5xx) {
    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(3, Duration.ofMillis(200), true, true);
    }
}
