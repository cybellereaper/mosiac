package imageboard.errors;

public class AuthenticationException extends ProviderException {
    public AuthenticationException(String provider, String message) {
        super(provider, message);
    }
}
