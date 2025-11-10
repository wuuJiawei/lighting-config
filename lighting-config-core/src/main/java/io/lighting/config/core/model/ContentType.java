package io.lighting.config.core.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Supported payload types for configuration values.
 */
public enum ContentType {
    TEXT("text/plain"),
    JSON("application/json"),
    YAML("application/x-yaml"),
    PROPERTIES("text/x-java-properties");

    private final String mimeType;

    ContentType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static ContentType fromAlias(String alias) {
        if (alias == null || alias.isEmpty()) {
            return TEXT;
        }
        final String normalized = alias.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(type -> type.name().equals(normalized))
                .findFirst()
                .orElse(TEXT);
    }

    public static Optional<ContentType> tryParse(String alias) {
        if (alias == null || alias.isEmpty()) {
            return Optional.empty();
        }
        final String normalized = alias.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(type -> type.name().equals(normalized))
                .findFirst();
    }
}
