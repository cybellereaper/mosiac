package imageboard.model;

import java.util.List;

public record SearchResult<T>(List<T> items, int page, int limit, boolean hasNextPage) {
}
