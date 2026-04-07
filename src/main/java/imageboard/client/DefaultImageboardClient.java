package imageboard.client;

import imageboard.api.ImageboardClient;
import imageboard.model.Post;
import imageboard.model.ProviderCapabilities;
import imageboard.model.SearchResult;
import imageboard.model.Tag;
import imageboard.providers.ProviderAdapter;
import imageboard.query.SearchQuery;

import java.util.Optional;

public final class DefaultImageboardClient implements ImageboardClient {
    private final ProviderAdapter adapter;

    public DefaultImageboardClient(ProviderAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public SearchResult<Post> searchPosts(SearchQuery query) {
        return adapter.searchPosts(query);
    }

    @Override
    public Optional<Post> getPostById(long postId) {
        return adapter.getPostById(postId);
    }

    @Override
    public SearchResult<Tag> searchTags(String name, int page, int limit) {
        return adapter.searchTags(name, page, limit);
    }

    @Override
    public ProviderCapabilities capabilities() {
        return adapter.capabilities();
    }
}
