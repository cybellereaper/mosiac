package imageboard.http;

import imageboard.errors.ImageboardException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class JdkHttpTransport implements HttpTransport {
    private final HttpClient client;

    public JdkHttpTransport(Duration connectTimeout) {
        this.client = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
    }

    @Override
    public HttpResponseData execute(HttpRequestData request) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(request.uri())
                    .timeout(request.readTimeout())
                    .GET();
            for (Map.Entry<String, String> e : request.headers().entrySet()) {
                builder.header(e.getKey(), e.getValue());
            }
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new HttpResponseData(response.statusCode(), response.body(), response.headers().map());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new ImageboardException("HTTP transport failure", e);
        }
    }
}
