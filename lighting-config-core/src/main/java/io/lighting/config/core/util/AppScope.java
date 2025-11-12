package io.lighting.config.core.util;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Small helper utilities to deal with app scope fan-out (per-app plus shared/global scopes).
 */
public final class AppScope {

    private AppScope() {
    }

    /**
     * Fallback app id used to denote globally shared configs.
     */
    public static final String GLOBAL_APP_ID = "__global__";

    /**
     * Parse a comma separated appId string into an ordered list without duplicates. Blanks are ignored.
     */
    public static List<String> parse(String raw) {
        LinkedHashSet<String> resolved = new LinkedHashSet<>();
        if (raw != null && !raw.isBlank()) {
            for (String token : raw.split(",")) {
                String value = token.trim();
                if (!value.isEmpty()) {
                    resolved.add(value);
                }
            }
        }
        if (resolved.isEmpty()) {
            resolved.add("default");
        }
        return List.copyOf(resolved);
    }

    /**
     * Parse an appId string and ensure the global scope is appended to the end.
     */
    public static List<String> parseWithGlobal(String raw) {
        LinkedHashSet<String> resolved = new LinkedHashSet<>(parse(raw));
        resolved.add(GLOBAL_APP_ID);
        return List.copyOf(resolved);
    }
}
