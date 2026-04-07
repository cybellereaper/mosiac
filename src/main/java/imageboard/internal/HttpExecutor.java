package imageboard.internal;

import imageboard.errors.AuthenticationException;
import imageboard.errors.RateLimitException;
import imageboard.errors.RemoteApiException;
import imageboard.http.HttpRequestData;
import imageboard.http.HttpResponseData;
import imageboard.http.HttpTransport;
import imageboard.http.RateLimiter;
import imageboard.http.RetryPolicy;

import java.time.Duration;

public final class HttpExecutor {
    private final HttpTransport transport;
    private final RetryPolicy retryPolicy;
    private final RateLimiter rateLimiter;

    public HttpExecutor(HttpTransport transport, RetryPolicy retryPolicy, RateLimiter rateLimiter) {
        this.transport = transport;
        this.retryPolicy = retryPolicy;
        this.rateLimiter = rateLimiter;
    }

    public HttpResponseData execute(String provider, HttpRequestData request) {
        int attempt = 0;
        while (true) {
            attempt++;
            rateLimiter.acquire();
            HttpResponseData response = transport.execute(request);
            int code = response.statusCode();
            if (code == 401 || code == 403) {
                throw new AuthenticationException(provider, "Authentication rejected");
            }
            if (code == 429) {
                if (attempt < retryPolicy.maxAttempts() && retryPolicy.retryOnRateLimit()) {
                    sleep(retryPolicy.backoff());
                    continue;
                }
                throw new RateLimitException(provider, "Rate limit exceeded", retryPolicy.backoff());
            }
            if (code >= 500 && attempt < retryPolicy.maxAttempts() && retryPolicy.retryOn5xx()) {
                sleep(retryPolicy.backoff());
                continue;
            }
            if (code >= 400) {
                throw new RemoteApiException(provider, code, "Remote API returned error status " + code);
            }
            return response;
        }
    }

    private void sleep(Duration backoff) {
        try {
            Thread.sleep(Math.max(1, backoff.toMillis()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
