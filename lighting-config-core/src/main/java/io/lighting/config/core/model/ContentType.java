package io.lighting.config.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Data type of a configuration value. Only primitive/collection semantics are described here;
 * actual parsing happens on the client side based on {@code contentType}.
 */
public enum ContentType {
    STRING,
    BOOLEAN,
    BYTE,
    SHORT,
    INTEGER,
    LONG,
    FLOAT,
    DOUBLE,
    LIST,
    MAP;

    private static final Map<String, ContentType> ALIASES;

    static {
        Map<String, ContentType> alias = new HashMap<>();
        for (ContentType type : values()) {
            alias.put(type.name(), type);
        }
        // Compatibility with legacy values and human-friendly shortcuts.
        alias.put("TEXT", STRING);
        alias.put("JSON", MAP);
        alias.put("OBJECT", MAP);
        alias.put("YAML", STRING);
        alias.put("PROPERTIES", STRING);
        alias.put("BOOL", BOOLEAN);
        alias.put("INT", INTEGER);
        alias.put("LONG", LONG);
        alias.put("FLOATING", FLOAT);
        alias.put("DECIMAL", DOUBLE);
        alias.put("ARRAY", LIST);
        ALIASES = Collections.unmodifiableMap(alias);
    }

    public static ContentType fromAlias(String alias) {
        if (alias == null || alias.isEmpty()) {
            return STRING;
        }
        final String normalized = alias.trim().toUpperCase(Locale.ROOT);
        return ALIASES.getOrDefault(normalized, STRING);
    }

    public static Optional<ContentType> tryParse(String alias) {
        if (alias == null || alias.isEmpty()) {
            return Optional.empty();
        }
        final String normalized = alias.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(ALIASES.get(normalized));
    }
}
