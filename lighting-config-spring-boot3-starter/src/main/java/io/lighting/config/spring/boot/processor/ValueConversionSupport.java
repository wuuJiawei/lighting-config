package io.lighting.config.spring.boot.processor;

import io.lighting.config.client.value.ValueDecoderRegistry;
import io.lighting.config.core.model.ContentType;
import org.springframework.core.convert.ConversionService;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

final class ValueConversionSupport {

    private ValueConversionSupport() {
    }

    static Object convert(String rawValue,
                          ContentType contentType,
                          Type targetType,
                          ValueDecoderRegistry decoderRegistry,
                          ConversionService conversionService,
                          String contextDescription) {
        if (targetType == null) {
            return rawValue;
        }
        Class<?> rawClass = resolveRawClass(targetType);
        if (rawClass == null || CharSequence.class.isAssignableFrom(rawClass)) {
            return rawValue;
        }
        if (rawValue == null) {
            return null;
        }
        Optional<Object> decoded = decoderRegistry.decode(rawValue, contentType, targetType);
        if (decoded.isPresent()) {
            return decoded.get();
        }
        if (conversionService != null && conversionService.canConvert(String.class, rawClass)) {
            return conversionService.convert(rawValue, rawClass);
        }
        throw new IllegalStateException("Unsupported type for " + contextDescription);
    }

    private static Class<?> resolveRawClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            Type raw = ((ParameterizedType) type).getRawType();
            if (raw instanceof Class<?>) {
                return (Class<?>) raw;
            }
        }
        if (type instanceof GenericArrayType) {
            Type component = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = resolveRawClass(component);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            }
        }
        return null;
    }
}
