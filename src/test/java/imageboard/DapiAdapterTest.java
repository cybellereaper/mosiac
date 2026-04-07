package imageboard;

import imageboard.auth.AuthConfig;
import imageboard.http.*;
import imageboard.internal.HttpExecutor;
import imageboard.model.Rating;
import imageboard.providers.gelbooru.GelbooruAdapter;
import imageboard.providers.rule34.Rule34Adapter;
import imageboard.query.SearchQuery;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DapiAdapterTest {
    @Test
    void gelbooruParsesXml() {
        FakeTransport transport = new FakeTransport(new HttpResponseData(200, TestSupport.fixture("gelbooru/posts.xml"), Map.of()));
        GelbooruAdapter adapter = new GelbooruAdapter("https://gelbooru.com",
                new HttpExecutor(transport, RetryPolicy.defaultPolicy(), () -> {}), AuthConfig.none());

        var result = adapter.searchPosts(SearchQuery.builder().limit(1).pid(0).build());
        assertEquals(1, result.items().size());
        assertEquals(Rating.EXPLICIT, result.items().getFirst().rating());
    }

    @Test
    void rule34ParsesXmlAndNormalizesGifMediaType() {
        FakeTransport transport = new FakeTransport(new HttpResponseData(200, TestSupport.fixture("rule34/posts.xml"), Map.of()));
        Rule34Adapter adapter = new Rule34Adapter("https://rule34.xxx",
                new HttpExecutor(transport, RetryPolicy.defaultPolicy(), () -> {}), AuthConfig.none());

        var result = adapter.searchPosts(SearchQuery.builder().limit(1).pid(0).build());
        assertEquals("image/gif", result.items().getFirst().mediaType());
    }
}
