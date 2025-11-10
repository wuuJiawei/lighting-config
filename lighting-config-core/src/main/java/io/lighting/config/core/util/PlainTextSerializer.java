package io.lighting.config.core.util;

/**
 * Minimal serializer suitable for text payloads or testing.
 */
public final class PlainTextSerializer implements Serializer {

    public static final PlainTextSerializer INSTANCE = new PlainTextSerializer();

    private PlainTextSerializer() {
    }

    @Override
    public <T> String serialize(T value) {
        return value == null ? "" : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(String payload, Class<T> targetType) {
        if (targetType == String.class) {
            return (T) payload;
        }
        throw new SerializationException("PlainTextSerializer only supports String target types");
    }
}
