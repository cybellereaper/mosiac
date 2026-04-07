package imageboard.http;

public interface RateLimiter {
    void acquire();
}
