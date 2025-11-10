package io.lighting.config.core.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A simple wrapper that preserves insertion order while exposing an immutable view.
 */
public final class LabelSet {

    private static final LabelSet EMPTY = new LabelSet(Collections.emptyMap());

    private final Map<String, String> values;

    private LabelSet(Map<String, String> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    public static LabelSet empty() {
        return EMPTY;
    }

    public static LabelSet of(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return empty();
        }
        Map<String, String> copy = new LinkedHashMap<>();
        labels.forEach((k, v) -> {
            if (k != null && v != null) {
                copy.put(k, v);
            }
        });
        if (copy.isEmpty()) {
            return empty();
        }
        return new LabelSet(copy);
    }

    public Map<String, String> asMap() {
        return values;
    }

    public String get(String key) {
        return values.get(key);
    }

    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    public LabelSet with(String key, String value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Map<String, String> copy = new LinkedHashMap<>(values);
        copy.put(key, value);
        return new LabelSet(copy);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public String toString() {
        return "LabelSet" + values;
    }
}
