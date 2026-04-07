package imageboard.errors;

public class ProviderException extends ImageboardException {
    private final String provider;

    public ProviderException(String provider, String message) {
        super(message);
        this.provider = provider;
    }

    public ProviderException(String provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
    }

    public String provider() { return provider; }
}
