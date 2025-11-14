package io.lighting.config.client.value;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

final class ValueTypeUtils {

    private ValueTypeUtils() {
    }

    static Class<?> rawClass(Type type) {
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
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = rawClass(componentType);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            }
        }
        return null;
    }

    static boolean isBoolean(Class<?> type) {
        return type == Boolean.class || type == boolean.class;
    }

    static boolean isByte(Class<?> type) {
        return type == Byte.class || type == byte.class;
    }

    static boolean isShort(Class<?> type) {
        return type == Short.class || type == short.class;
    }

    static boolean isInteger(Class<?> type) {
        return type == Integer.class || type == int.class;
    }

    static boolean isLong(Class<?> type) {
        return type == Long.class || type == long.class;
    }

    static boolean isFloat(Class<?> type) {
        return type == Float.class || type == float.class;
    }

    static boolean isDouble(Class<?> type) {
        return type == Double.class || type == double.class;
    }

    static boolean isChar(Class<?> type) {
        return type == Character.class || type == char.class;
    }

    static boolean isScalarWrapper(Class<?> type) {
        return isBoolean(type) || isByte(type) || isShort(type) || isInteger(type)
                || isLong(type) || isFloat(type) || isDouble(type) || isChar(type)
                || Number.class.isAssignableFrom(type);
    }
}
