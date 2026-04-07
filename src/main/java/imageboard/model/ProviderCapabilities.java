package imageboard.model;

import java.util.Set;

public record ProviderCapabilities(
        boolean supportsTagSearch,
        boolean supportsSortOrder,
        boolean supportsAuthentication,
        Set<String> supportedSortValues
) {
}
