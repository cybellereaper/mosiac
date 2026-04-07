package imageboard.errors;

public class ImageboardException extends RuntimeException {
    public ImageboardException(String message) { super(message); }
    public ImageboardException(String message, Throwable cause) { super(message, cause); }
}
