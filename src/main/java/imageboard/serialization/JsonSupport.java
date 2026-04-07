package imageboard.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import imageboard.errors.SerializationException;

public final class JsonSupport {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonSupport() {}

    public static <T> T read(String json, TypeReference<T> typeReference) {
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new SerializationException("JSON parse failure", e);
        }
    }
}
