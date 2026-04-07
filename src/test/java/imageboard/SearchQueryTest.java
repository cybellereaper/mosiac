package imageboard;

import imageboard.query.SearchQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchQueryTest {
    @Test
    void builderCollectsTagsAndOptions() {
        SearchQuery query = SearchQuery.builder()
                .addTag(" cat ")
                .addTag("")
                .page(2)
                .limit(50)
                .sort("score")
                .option("foo", "bar")
                .build();

        assertEquals(1, query.tags().size());
        assertEquals("cat", query.tags().getFirst());
        assertEquals(2, query.page());
        assertEquals(50, query.limit());
        assertEquals("score", query.sort());
        assertEquals("bar", query.providerOptions().get("foo"));
    }
}
