package io.lighting.config.spring.boot.processor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class KeyVariantUtils {

    private KeyVariantUtils() {
    }

    static List<String> fieldVariants(String prefix, String fieldName) {
        Set<String> variants = new LinkedHashSet<>();
        String safePrefix = prefix == null ? "" : prefix;
        addVariant(variants, safePrefix + fieldName);
        addVariant(variants, safePrefix + normalizeExplicit(fieldName));
        addVariant(variants, safePrefix + segmentVariant(fieldName, '.'));
        addVariant(variants, safePrefix + segmentVariant(fieldName, '-'));
        addVariant(variants, safePrefix + segmentVariant(fieldName, '_'));
        return new ArrayList<>(variants);
    }

    static List<String> explicitKeyVariants(String key) {
        if (key == null || key.isEmpty()) {
            return List.of();
        }
        Set<String> variants = new LinkedHashSet<>();
        variants.add(key);
        variants.add(convertKey(key, '-'));
        variants.add(convertKey(key, '_'));
        return new ArrayList<>(variants);
    }

    private static String normalizeExplicit(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == '-' || ch == '_') {
                builder.append('.');
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private static String segmentVariant(String name, char delimiter) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                builder.append(delimiter).append(Character.toLowerCase(ch));
            } else if (ch == '_' || ch == '-') {
                builder.append(delimiter);
            } else {
                builder.append(ch);
            }
        }
        if (builder.length() > 0 && builder.charAt(0) == delimiter) {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }

    private static String convertKey(String key, char segmentDelimiter) {
        StringBuilder builder = new StringBuilder();
        char previous = 0;
        for (int i = 0; i < key.length(); i++) {
            char ch = key.charAt(i);
            if (ch == '.') {
                builder.append('.');
                previous = '.';
                continue;
            }
            if (ch == '-' || ch == '_') {
                builder.append(segmentDelimiter);
                previous = segmentDelimiter;
                continue;
            }
            if (Character.isUpperCase(ch)) {
                if (previous != '.' && previous != segmentDelimiter && builder.length() > 0) {
                    builder.append(segmentDelimiter);
                }
                builder.append(Character.toLowerCase(ch));
                previous = ch;
                continue;
            }
            builder.append(ch);
            previous = ch;
        }
        return builder.toString();
    }

    private static void addVariant(Set<String> variants, String candidate) {
        if (candidate != null && !candidate.isEmpty()) {
            variants.add(candidate);
        }
    }
}
