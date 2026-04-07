package imageboard.errors;

import java.time.Duration;

public class RateLimitException extends ProviderException {
    private final Duration retryAfter;

    public RateLimitException(String provider, String message, Duration retryAfter) {
        super(provider, message);
        this.retryAfter = retryAfter;
    }

    public Duration retryAfter() {
        return retryAfter;
    }
}
