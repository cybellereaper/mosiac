package imageboard.model;

import java.util.Map;

public record ProviderMetadata(String providerId, Map<String, String> fields, String rawPayload) {
}
