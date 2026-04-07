package imageboard.app;

import imageboard.api.ImageboardClient;
import imageboard.api.ImageboardProvider;
import imageboard.model.Post;
import imageboard.model.ProviderCapabilities;
import imageboard.model.ProviderMetadata;
import imageboard.model.Rating;
import imageboard.model.SearchResult;
import imageboard.model.Tag;
import imageboard.query.SearchQuery;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void printsUsageWhenNoArgs() {
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        int status = Main.run(new String[0], new PrintStream(outBuffer), new PrintStream(new ByteArrayOutputStream()),
                cfg -> fail("Client factory should not be called for help"));

        assertEquals(0, status);
        assertTrue(outBuffer.toString().contains("Usage: Main"));
    }

    @Test
    void returnsErrorForUnknownProvider() {
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
        int status = Main.run(new String[]{"badprovider"}, new PrintStream(new ByteArrayOutputStream()), new PrintStream(errBuffer),
                cfg -> fail("Client factory should not be called for parse errors"));

        assertEquals(2, status);
        assertTrue(errBuffer.toString().contains("Unknown provider"));
    }

    @Test
    void runsSearchAndPrintsResults() {
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        int status = Main.run(new String[]{"danbooru", "cat", "--limit=5"},
                new PrintStream(outBuffer),
                new PrintStream(new ByteArrayOutputStream()),
                cfg -> new FakeClient());

        assertEquals(0, status);
        String output = outBuffer.toString();
        assertTrue(output.contains("provider=danbooru"));
        assertTrue(output.contains("count=1"));
        assertTrue(output.contains("101"));
    }

    private static final class FakeClient implements ImageboardClient {
        @Override
        public SearchResult<Post> searchPosts(SearchQuery query) {
            Post post = new Post(101L, null, null, List.of("cat"), null,
                    Rating.SAFE, 1, 100, 100, "https://example.test/a.jpg", null, null,
                    "jpg", "image/jpeg", new ProviderMetadata("danbooru", Map.of(), null));
            return new SearchResult<>(List.of(post), 1, query.limit() == null ? 20 : query.limit(), false);
        }

        @Override
        public Optional<Post> getPostById(long postId) {
            return Optional.empty();
        }

        @Override
        public SearchResult<Tag> searchTags(String name, int page, int limit) {
            return new SearchResult<>(List.of(), page, limit, false);
        }

        @Override
        public ProviderCapabilities capabilities() {
            return new ProviderCapabilities(true, true, false, java.util.Set.of());
        }
    }
}
