package io.lighting.config.client.value;

import io.lighting.config.core.model.ContentType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Ordered registry that tries registered {@link ConfigValueDecoder}s one by one.
 */
public final class ValueDecoderRegistry {

    private final List<ConfigValueDecoder> decoders;

    private ValueDecoderRegistry(List<ConfigValueDecoder> decoders) {
        this.decoders = decoders;
    }

    public static ValueDecoderRegistry withDefaults(List<ConfigValueDecoder> customDecoders) {
        List<ConfigValueDecoder> ordered = new ArrayList<>();
        if (customDecoders != null && !customDecoders.isEmpty()) {
            ordered.addAll(customDecoders);
        }
        ordered.add(new ScalarValueDecoder());
        ordered.add(new JacksonValueDecoder());
        return new ValueDecoderRegistry(Collections.unmodifiableList(ordered));
    }

    public Optional<Object> decode(String rawValue, ContentType contentType, Type targetType) {
        if (rawValue == null) {
            return Optional.empty();
        }
        for (ConfigValueDecoder decoder : decoders) {
            if (decoder.supports(contentType, targetType)) {
                Object decoded = decoder.decode(rawValue, contentType, targetType);
                if (decoded != null) {
                    return Optional.of(decoded);
                }
            }
        }
        return Optional.empty();
    }
}
