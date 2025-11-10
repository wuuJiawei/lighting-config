package io.lighting.config.core.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Authoritative configuration entity stored in the repository.
 */
public final class ConfigItem {

    private final String tenant;
    private final String namespace;
    private final String appId;
    private final String key;
    private final ContentType contentType;
    private final String value;
    private final long version;
    private final Map<String, String> labels;
    private final boolean enabled;
    private final Instant createdAt;
    private final Instant updatedAt;

    private ConfigItem(Builder builder) {
        this.tenant = Objects.requireNonNull(builder.tenant, "tenant");
        this.namespace = Objects.requireNonNull(builder.namespace, "namespace");
        this.appId = Objects.requireNonNull(builder.appId, "appId");
        this.key = Objects.requireNonNull(builder.key, "key");
        this.contentType = Objects.requireNonNull(builder.contentType, "contentType");
        this.value = Objects.requireNonNull(builder.value, "value");
        this.version = builder.version;
        this.labels = Collections.unmodifiableMap(new LinkedHashMap<>(builder.labels));
        this.enabled = builder.enabled;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(builder.updatedAt, "updatedAt");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTenant() {
        return tenant;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getAppId() {
        return appId;
    }

    public String getKey() {
        return key;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getValue() {
        return value;
    }

    public long getVersion() {
        return version;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Builder toBuilder() {
        return builder()
                .tenant(tenant)
                .namespace(namespace)
                .appId(appId)
                .key(key)
                .contentType(contentType)
                .value(value)
                .version(version)
                .labels(labels)
                .enabled(enabled)
                .createdAt(createdAt)
                .updatedAt(updatedAt);
    }

    @Override
    public String toString() {
        return "ConfigItem{" +
                "tenant='" + tenant + '\'' +
                ", namespace='" + namespace + '\'' +
                ", appId='" + appId + '\'' +
                ", key='" + key + '\'' +
                ", version=" + version +
                ", enabled=" + enabled +
                '}';
    }

    public static final class Builder {
        private String tenant;
        private String namespace;
        private String appId;
        private String key;
        private ContentType contentType = ContentType.TEXT;
        private String value = "";
        private long version = 0L;
        private Map<String, String> labels = new LinkedHashMap<>();
        private boolean enabled = true;
        private Instant createdAt = Instant.EPOCH;
        private Instant updatedAt = Instant.EPOCH;

        private Builder() {
        }

        public Builder tenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public Builder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder contentType(ContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder version(long version) {
            this.version = version;
            return this;
        }

        public Builder labels(Map<String, String> labels) {
            this.labels = labels == null ? new LinkedHashMap<>() : new LinkedHashMap<>(labels);
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ConfigItem build() {
            return new ConfigItem(this);
        }
    }
}
