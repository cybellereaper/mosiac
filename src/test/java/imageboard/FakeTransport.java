package imageboard;

import imageboard.http.HttpRequestData;
import imageboard.http.HttpResponseData;
import imageboard.http.HttpTransport;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public final class FakeTransport implements HttpTransport {
    private final Queue<HttpResponseData> responses;
    private HttpRequestData lastRequest;

    public FakeTransport(HttpResponseData... responses) {
        this.responses = new ArrayDeque<>(List.of(responses));
    }

    @Override
    public HttpResponseData execute(HttpRequestData request) {
        lastRequest = request;
        HttpResponseData data = responses.poll();
        if (data == null) {
            return new HttpResponseData(200, "[]", Map.of());
        }
        return data;
    }

    public HttpRequestData lastRequest() {
        return lastRequest;
    }
}
