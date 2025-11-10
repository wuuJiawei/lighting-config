package io.lighting.config.core.spi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Context passed to repository factories, containing configuration resolved from the server.
 */
public final class StorageProviderContext {

    private final StorageType storageType;
    private final Map<String, String> settings;
    private final Properties properties;

    private StorageProviderContext(Builder builder) {
        this.storageType = Objects.requireNonNull(builder.storageType, "storageType");
        this.settings = Collections.unmodifiableMap(new LinkedHashMap<>(builder.settings));
        this.properties = new Properties();
        this.properties.putAll(builder.properties);
    }

    public static Builder builder() {
        return new Builder();
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public Properties getProperties() {
        Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }

    public String require(String key) {
        String value = settings.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing storage setting: " + key);
        }
        return value;
    }

    public static final class Builder {
        private StorageType storageType = StorageType.JDBC;
        private Map<String, String> settings = new LinkedHashMap<>();
        private Properties properties = new Properties();

        public Builder storageType(StorageType storageType) {
            this.storageType = storageType;
            return this;
        }

        public Builder settings(Map<String, String> settings) {
            this.settings = settings == null ? new LinkedHashMap<>() : new LinkedHashMap<>(settings);
            return this;
        }

        public Builder properties(Properties properties) {
            this.properties = properties == null ? new Properties() : properties;
            return this;
        }

        public StorageProviderContext build() {
            return new StorageProviderContext(this);
        }
    }
}
