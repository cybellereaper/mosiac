package imageboard.model;

import java.time.Instant;
import java.util.List;

public record Post(
        long id,
        Instant createdAt,
        Instant updatedAt,
        List<String> tags,
        String source,
        Rating rating,
        Integer score,
        Integer width,
        Integer height,
        String fileUrl,
        String previewUrl,
        String sampleUrl,
        String fileExtension,
        String mediaType,
        ProviderMetadata providerMetadata
) {
}
