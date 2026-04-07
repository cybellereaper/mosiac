package imageboard.api;

import imageboard.model.Post;
import imageboard.model.ProviderCapabilities;
import imageboard.model.SearchResult;
import imageboard.model.Tag;
import imageboard.query.SearchQuery;

import java.util.Optional;

public interface ImageboardClient {
    SearchResult<Post> searchPosts(SearchQuery query);
    Optional<Post> getPostById(long postId);
    SearchResult<Tag> searchTags(String name, int page, int limit);
    ProviderCapabilities capabilities();
}
