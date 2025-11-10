package io.lighting.config.core.spi;

import java.util.Locale;

/**
 * Supported storage backends for the repository SPI.
 */
public enum StorageType {
    JDBC,
    FILE,
    CUSTOM;

    public static StorageType from(String value) {
        if (value == null || value.isEmpty()) {
            return JDBC;
        }
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
