package imageboard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestSupport {
    private TestSupport() {}

    public static String fixture(String path) {
        try {
            return Files.readString(Path.of("src/test/resources/fixtures/" + path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
