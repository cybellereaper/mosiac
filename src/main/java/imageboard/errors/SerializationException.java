package imageboard.errors;

public class SerializationException extends ImageboardException {
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
