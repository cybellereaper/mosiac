package imageboard.api;

import imageboard.auth.AuthConfig;
import imageboard.client.DefaultImageboardClient;
import imageboard.http.FixedWindowRateLimiter;
import imageboard.http.JdkHttpTransport;
import imageboard.internal.HttpExecutor;
import imageboard.providers.ProviderAdapter;
import imageboard.providers.danbooru.DanbooruAdapter;
import imageboard.providers.gelbooru.GelbooruAdapter;
import imageboard.providers.rule34.Rule34Adapter;

import java.util.HashMap;
import java.util.Map;

public final class ImageboardClients {
    private ImageboardClients() {}

    public static Builder builder(ImageboardProvider provider) {
        return new Builder(provider);
    }

    public static final class Builder {
        private final ImageboardProvider provider;
        private String baseUrl;
        private AuthConfig authConfig = AuthConfig.none();
        private RequestOptions requestOptions = RequestOptions.defaults();

        private Builder(ImageboardProvider provider) {
            this.provider = provider;
            this.baseUrl = defaultBaseUrl(provider);
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder auth(AuthConfig authConfig) {
            this.authConfig = authConfig;
            return this;
        }

        public Builder requestOptions(RequestOptions requestOptions) {
            this.requestOptions = requestOptions;
            return this;
        }

        public ImageboardClient build() {
            var transport = new JdkHttpTransport(requestOptions.connectTimeout());
            var executor = new HttpExecutor(transport, requestOptions.retryPolicy(), new FixedWindowRateLimiter(requestOptions.rateLimitPolicy()));
            Map<String, String> headers = new HashMap<>(requestOptions.defaultHeaders());
            headers.put("User-Agent", requestOptions.userAgent());
            ProviderAdapter adapter = switch (provider) {
                case DANBOORU -> new DanbooruAdapter(baseUrl, executor, authConfig, headers);
                case GELBOORU -> new GelbooruAdapter(baseUrl, executor, authConfig);
                case RULE34 -> new Rule34Adapter(baseUrl, executor, authConfig);
            };
            return new DefaultImageboardClient(adapter);
        }

        private static String defaultBaseUrl(ImageboardProvider provider) {
            return switch (provider) {
                case DANBOORU -> "https://danbooru.donmai.us";
                case GELBOORU -> "https://gelbooru.com";
                case RULE34 -> "https://rule34.xxx";
            };
        }
    }
}
