package imageboard.auth;

public sealed interface AuthConfig permits AuthConfig.None, AuthConfig.ApiKey {
    record None() implements AuthConfig {}
    record ApiKey(String username, String apiKey) implements AuthConfig {}

    static AuthConfig none() { return new None(); }
    static AuthConfig apiKey(String username, String apiKey) { return new ApiKey(username, apiKey); }
}
