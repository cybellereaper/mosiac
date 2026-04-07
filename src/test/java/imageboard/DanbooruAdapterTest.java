package imageboard;

import imageboard.auth.AuthConfig;
import imageboard.http.*;
import imageboard.internal.HttpExecutor;
import imageboard.model.Rating;
import imageboard.providers.danbooru.DanbooruAdapter;
import imageboard.query.SearchQuery;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DanbooruAdapterTest {
    @Test
    void parsesPostListIntoNormalizedModel() {
        FakeTransport transport = new FakeTransport(new HttpResponseData(200, TestSupport.fixture("danbooru/posts.json"), Map.of()));
        DanbooruAdapter adapter = new DanbooruAdapter("https://danbooru.donmai.us",
                new HttpExecutor(transport, RetryPolicy.defaultPolicy(), () -> {}), AuthConfig.none(), Map.of());

        var result = adapter.searchPosts(SearchQuery.builder().addTag("cat").limit(1).page(1).build());
        assertEquals(1, result.items().size());
        var post = result.items().getFirst();
        assertEquals(123L, post.id());
        assertEquals(Rating.SAFE, post.rating());
        assertEquals("jpg", post.fileExtension());
        assertEquals("image/jpeg", post.mediaType());
    }

    @Test
    void appendsAuthToRequestWhenConfigured() {
        FakeTransport transport = new FakeTransport(new HttpResponseData(200, TestSupport.fixture("danbooru/posts.json"), Map.of()));
        DanbooruAdapter adapter = new DanbooruAdapter("https://danbooru.donmai.us",
                new HttpExecutor(transport, RetryPolicy.defaultPolicy(), () -> {}), AuthConfig.apiKey("user", "secret"), Map.of());

        adapter.searchPosts(SearchQuery.builder().build());
        String uri = transport.lastRequest().uri().toString();
        assertTrue(uri.contains("login=user"));
        assertTrue(uri.contains("api_key=secret"));
    }
}
