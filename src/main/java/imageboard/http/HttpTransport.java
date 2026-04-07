package imageboard.http;

public interface HttpTransport {
    HttpResponseData execute(HttpRequestData request);
}
