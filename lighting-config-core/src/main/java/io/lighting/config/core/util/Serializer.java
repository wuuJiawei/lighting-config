package io.lighting.config.core.util;

/**
 * Generic serializer abstraction used for persisting metadata fields.
 */
public interface Serializer {

    <T> String serialize(T value) throws SerializationException;

    <T> T deserialize(String payload, Class<T> targetType) throws SerializationException;
}
