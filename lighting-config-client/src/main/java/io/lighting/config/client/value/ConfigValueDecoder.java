package io.lighting.config.client.value;

import io.lighting.config.core.model.ContentType;

import java.lang.reflect.Type;

/**
 * Strategy interface for converting string payloads into strongly typed values.
 */
public interface ConfigValueDecoder {

    /**
     * @return true if this decoder can handle the given combination of content type and target type.
     */
    boolean supports(ContentType contentType, Type targetType);

    /**
     * Decode the raw string into the desired type.
     */
    Object decode(String rawValue, ContentType contentType, Type targetType);
}
