package imageboard.model;

public enum Rating {
    SAFE,
    QUESTIONABLE,
    EXPLICIT,
    UNKNOWN;

    public static Rating fromProviderValue(String value) {
        if (value == null || value.isBlank()) return UNKNOWN;
        return switch (value.trim().toLowerCase()) {
            case "s", "safe", "general" -> SAFE;
            case "q", "questionable" -> QUESTIONABLE;
            case "e", "explicit", "adult" -> EXPLICIT;
            default -> UNKNOWN;
        };
    }
}
