# Mosiac Imageboard API Library

A Java 25 library for querying Danbooru-style imageboard APIs through one normalized client API.

## Supported providers (this pass)
- Danbooru (JSON API)
- Gelbooru (DAPI XML)
- Rule34 (DAPI XML, separate adapter)

## Quick start

```java
import imageboard.api.*;
import imageboard.auth.AuthConfig;
import imageboard.query.SearchQuery;

ImageboardClient danbooru = ImageboardClients.builder(ImageboardProvider.DANBOORU)
        .auth(AuthConfig.apiKey("username", System.getenv("DANBOORU_API_KEY")))
        .build();

var posts = danbooru.searchPosts(SearchQuery.builder()
        .addTag("cat")
        .addTag("rating:safe")
        .limit(20)
        .page(1)
        .sort("score")
        .build());

posts.items().forEach(post -> {
    System.out.println(post.id() + " " + post.fileUrl() + " " + post.rating());
    // provider-specific fields remain available under metadata
    System.out.println(post.providerMetadata().fields());
});

var one = danbooru.getPostById(123L);
```

Switching providers:

```java
ImageboardClient gelbooru = ImageboardClients.builder(ImageboardProvider.GELBOORU).build();
ImageboardClient rule34 = ImageboardClients.builder(ImageboardProvider.RULE34).build();
```

Calling code remains mostly identical because results map to the same normalized `Post` / `Tag` models.

## Provider differences and notes
- Pagination:
  - Danbooru uses `page`.
  - Gelbooru/Rule34 DAPI uses `pid`.
- Sort:
  - Danbooru supports explicit sort values in this pass.
  - Gelbooru/Rule34 sort support is provider-limited; capability advertises unsupported behavior.
- Auth:
  - Danbooru supports `login` + `api_key` query auth in this pass.
  - Gelbooru/Rule34 commonly work unauthenticated for read endpoints.
- Unsupported operations fail explicitly through typed errors, rather than silently ignoring options.

## Public API surface
- `ImageboardClient`
- `ImageboardProvider`
- `SearchQuery`
- `SearchResult<T>`
- `Post`
- `Tag`
- `Rating`
- `ProviderCapabilities`
- `AuthConfig`
- `RequestOptions`

## Scope of this pass
Read/query-only operations:
- post search
- post by ID lookup
- tag search
- normalized mapping + provider metadata preservation
- retry and rate-limit aware request execution
