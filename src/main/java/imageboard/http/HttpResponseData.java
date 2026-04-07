package imageboard.http;

import java.util.List;
import java.util.Map;

public record HttpResponseData(int statusCode, String body, Map<String, List<String>> headers) {
}
