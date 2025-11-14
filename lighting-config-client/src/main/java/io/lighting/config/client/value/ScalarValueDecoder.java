package io.lighting.config.client.value;

import io.lighting.config.core.model.ContentType;

import java.lang.reflect.Type;

final class ScalarValueDecoder implements ConfigValueDecoder {

    @Override
    public boolean supports(ContentType contentType, Type targetType) {
        Class<?> rawClass = ValueTypeUtils.rawClass(targetType);
        if (rawClass == null) {
            return false;
        }
        if (CharSequence.class.isAssignableFrom(rawClass)) {
            return true;
        }
        if (rawClass.isEnum()) {
            return true;
        }
        return rawClass.isPrimitive() || ValueTypeUtils.isScalarWrapper(rawClass);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object decode(String rawValue, ContentType contentType, Type targetType) {
        Class<?> rawClass = ValueTypeUtils.rawClass(targetType);
        if (rawClass == null) {
            return null;
        }
        if (CharSequence.class.isAssignableFrom(rawClass)) {
            return rawValue;
        }
        if (rawClass.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) rawClass, rawValue.trim());
        }
        String trimmed = rawValue == null ? null : rawValue.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            if (rawClass.isPrimitive()) {
                return DefaultPrimitiveValue.of(rawClass);
            }
            return null;
        }
        if (ValueTypeUtils.isBoolean(rawClass)) {
            return Boolean.parseBoolean(trimmed);
        }
        if (ValueTypeUtils.isByte(rawClass)) {
            return Byte.parseByte(trimmed);
        }
        if (ValueTypeUtils.isShort(rawClass)) {
            return Short.parseShort(trimmed);
        }
        if (ValueTypeUtils.isInteger(rawClass)) {
            return Integer.parseInt(trimmed);
        }
        if (ValueTypeUtils.isLong(rawClass)) {
            return Long.parseLong(trimmed);
        }
        if (ValueTypeUtils.isFloat(rawClass)) {
            return Float.parseFloat(trimmed);
        }
        if (ValueTypeUtils.isDouble(rawClass)) {
            return Double.parseDouble(trimmed);
        }
        if (ValueTypeUtils.isChar(rawClass)) {
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Cannot convert empty value to char");
            }
            return trimmed.charAt(0);
        }
        if (Number.class.isAssignableFrom(rawClass)) {
            return parseNumber(trimmed, (Class<? extends Number>) rawClass);
        }
        return null;
    }

    private static Number parseNumber(String value, Class<? extends Number> targetType) {
        if (targetType == Byte.class) {
            return Byte.valueOf(value);
        }
        if (targetType == Short.class) {
            return Short.valueOf(value);
        }
        if (targetType == Integer.class) {
            return Integer.valueOf(value);
        }
        if (targetType == Long.class) {
            return Long.valueOf(value);
        }
        if (targetType == Float.class) {
            return Float.valueOf(value);
        }
        if (targetType == Double.class) {
            return Double.valueOf(value);
        }
        throw new IllegalArgumentException("Unsupported numeric type: " + targetType.getName());
    }

    private static final class DefaultPrimitiveValue {
        private DefaultPrimitiveValue() {
        }

        static Object of(Class<?> primitiveType) {
            if (primitiveType == boolean.class) {
                return false;
            }
            if (primitiveType == byte.class) {
                return (byte) 0;
            }
            if (primitiveType == short.class) {
                return (short) 0;
            }
            if (primitiveType == int.class) {
                return 0;
            }
            if (primitiveType == long.class) {
                return 0L;
            }
            if (primitiveType == float.class) {
                return 0f;
            }
            if (primitiveType == double.class) {
                return 0d;
            }
            if (primitiveType == char.class) {
                return '\0';
            }
            throw new IllegalArgumentException("Unknown primitive type: " + primitiveType.getName());
        }
    }
}
