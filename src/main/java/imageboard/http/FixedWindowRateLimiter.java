package imageboard.http;

import java.time.Clock;

public final class FixedWindowRateLimiter implements RateLimiter {
    private final int max;
    private final long windowMs;
    private final Clock clock;
    private int used;
    private long windowStart;

    public FixedWindowRateLimiter(RateLimitPolicy policy) {
        this(policy, Clock.systemUTC());
    }

    FixedWindowRateLimiter(RateLimitPolicy policy, Clock clock) {
        this.max = Math.max(1, policy.maxRequestsPerWindow());
        this.windowMs = Math.max(1, policy.window().toMillis());
        this.clock = clock;
        this.windowStart = clock.millis();
    }

    @Override
    public synchronized void acquire() {
        long now = clock.millis();
        long elapsed = now - windowStart;
        if (elapsed >= windowMs) {
            windowStart = now;
            used = 0;
        }
        if (used >= max) {
            long sleepMs = windowMs - elapsed;
            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            windowStart = clock.millis();
            used = 0;
        }
        used++;
    }
}
