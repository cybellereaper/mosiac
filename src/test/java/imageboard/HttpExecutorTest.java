package imageboard;

import imageboard.errors.AuthenticationException;
import imageboard.errors.RateLimitException;
import imageboard.http.*;
import imageboard.internal.HttpExecutor;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpExecutorTest {
    @Test
    void retriesOn429ThenSucceeds() {
        FakeTransport transport = new FakeTransport(
                new HttpResponseData(429, "", Map.of()),
                new HttpResponseData(200, "ok", Map.of())
        );
        HttpExecutor executor = new HttpExecutor(transport, new RetryPolicy(2, Duration.ofMillis(1), true, true), () -> {});

        var response = executor.execute("danbooru", new HttpRequestData(HttpMethod.GET, URI.create("https://x.test"), Map.of(), Duration.ofSeconds(1)));
        assertEquals(200, response.statusCode());
    }

    @Test
    void throwsAuthenticationExceptionOn401() {
        FakeTransport transport = new FakeTransport(new HttpResponseData(401, "", Map.of()));
        HttpExecutor executor = new HttpExecutor(transport, RetryPolicy.defaultPolicy(), () -> {});

        assertThrows(AuthenticationException.class, () ->
                executor.execute("danbooru", new HttpRequestData(HttpMethod.GET, URI.create("https://x.test"), Map.of(), Duration.ofSeconds(1))));
    }

    @Test
    void throwsRateLimitExceptionWhenRetriesExhausted() {
        FakeTransport transport = new FakeTransport(
                new HttpResponseData(429, "", Map.of()),
                new HttpResponseData(429, "", Map.of())
        );
        HttpExecutor executor = new HttpExecutor(transport, new RetryPolicy(2, Duration.ofMillis(1), true, true), () -> {});

        assertThrows(RateLimitException.class, () ->
                executor.execute("gelbooru", new HttpRequestData(HttpMethod.GET, URI.create("https://x.test"), Map.of(), Duration.ofSeconds(1))));
    }
}
