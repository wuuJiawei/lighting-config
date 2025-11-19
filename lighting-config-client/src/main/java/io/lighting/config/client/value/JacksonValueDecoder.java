package io.lighting.config.client.value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lighting.config.core.model.ContentType;

import java.lang.reflect.Type;
import java.util.Map;

final class JacksonValueDecoder implements ConfigValueDecoder {

    private final ObjectMapper objectMapper;

    JacksonValueDecoder() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public boolean supports(ContentType contentType, Type targetType) {
        Class<?> rawClass = ValueTypeUtils.rawClass(targetType);
        if (rawClass == null) {
            return false;
        }
        if (Iterable.class.isAssignableFrom(rawClass) || rawClass.isArray()) {
            return contentType == ContentType.LIST || contentType == ContentType.MAP || contentType == ContentType.STRING;
        }
        if (Map.class.isAssignableFrom(rawClass)) {
            return true;
        }
        boolean complexPojo = !rawClass.isPrimitive()
                && !ValueTypeUtils.isScalarWrapper(rawClass)
                && !CharSequence.class.isAssignableFrom(rawClass)
                && !rawClass.isEnum();
        if (complexPojo) {
            return contentType == ContentType.MAP || contentType == ContentType.LIST;
        }
        return contentType == ContentType.MAP || contentType == ContentType.LIST;
    }

    @Override
    public Object decode(String rawValue, ContentType contentType, Type targetType) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        JavaType javaType = objectMapper.getTypeFactory().constructType(targetType);
        try {
            return objectMapper.readValue(rawValue, javaType);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to decode config value as JSON", e);
        }
    }
}
