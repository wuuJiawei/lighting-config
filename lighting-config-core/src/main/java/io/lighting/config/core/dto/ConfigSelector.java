package io.lighting.config.core.dto;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents either a single key or a prefix filter.
 */
public final class ConfigSelector {

    private final String key;
    private final String prefix;

    private ConfigSelector(String key, String prefix) {
        this.key = key;
        this.prefix = prefix;
    }

    public static ConfigSelector byKey(String key) {
        return new ConfigSelector(Objects.requireNonNull(key, "key"), null);
    }

    public static ConfigSelector byPrefix(String prefix) {
        return new ConfigSelector(null, Objects.requireNonNull(prefix, "prefix"));
    }

    public static ConfigSelector wildcard() {
        return new ConfigSelector(null, null);
    }

    public Optional<String> getKey() {
        return Optional.ofNullable(key);
    }

    public Optional<String> getPrefix() {
        return Optional.ofNullable(prefix);
    }

    public boolean isWildcard() {
        return key == null && prefix == null;
    }
}
