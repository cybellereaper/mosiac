package imageboard.errors;

public class RemoteApiException extends ProviderException {
    private final int statusCode;

    public RemoteApiException(String provider, int statusCode, String message) {
        super(provider, message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
