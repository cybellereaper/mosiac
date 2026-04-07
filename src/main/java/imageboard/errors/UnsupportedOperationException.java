package imageboard.errors;

public class UnsupportedOperationException extends ProviderException {
    public UnsupportedOperationException(String provider, String message) {
        super(provider, message);
    }
}
