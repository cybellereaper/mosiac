package imageboard.model;

public record Tag(
        long id,
        String name,
        Integer postCount,
        String category,
        ProviderMetadata providerMetadata
) {
}
