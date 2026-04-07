package imageboard.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record SearchQuery(
        List<String> tags,
        Integer page,
        Integer pid,
        Integer limit,
        String sort,
        Map<String, String> providerOptions
) {
    public SearchQuery {
        tags = tags == null ? List.of() : List.copyOf(tags);
        providerOptions = providerOptions == null ? Map.of() : Map.copyOf(providerOptions);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<String> tags = new ArrayList<>();
        private final Map<String, String> providerOptions = new LinkedHashMap<>();
        private Integer page;
        private Integer pid;
        private Integer limit;
        private String sort;

        public Builder addTag(String tag) {
            if (tag != null && !tag.isBlank()) tags.add(tag.trim());
            return this;
        }

        public Builder tags(List<String> values) {
            tags.clear();
            if (values != null) {
                values.stream().filter(v -> v != null && !v.isBlank()).map(String::trim).forEach(tags::add);
            }
            return this;
        }

        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        public Builder pid(Integer pid) {
            this.pid = pid;
            return this;
        }

        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public Builder sort(String sort) {
            this.sort = sort;
            return this;
        }

        public Builder option(String key, String value) {
            if (key != null && !key.isBlank() && value != null) {
                providerOptions.put(key, value);
            }
            return this;
        }

        public SearchQuery build() {
            return new SearchQuery(Collections.unmodifiableList(new ArrayList<>(tags)), page, pid, limit, sort,
                    Collections.unmodifiableMap(new LinkedHashMap<>(providerOptions)));
        }
    }
}
