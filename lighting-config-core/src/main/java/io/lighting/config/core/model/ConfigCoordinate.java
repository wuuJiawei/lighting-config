package io.lighting.config.core.model;

import java.util.Objects;

/**
 * Identifies a configuration scope uniquely (tenant + namespace + app + key).
 */
public final class ConfigCoordinate {

    private final String tenant;
    private final String namespace;
    private final String appId;
    private final String key;

    private ConfigCoordinate(Builder builder) {
        this.tenant = Objects.requireNonNull(builder.tenant, "tenant");
        this.namespace = Objects.requireNonNull(builder.namespace, "namespace");
        this.appId = Objects.requireNonNull(builder.appId, "appId");
        this.key = Objects.requireNonNull(builder.key, "key");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ConfigCoordinate of(String tenant, String namespace, String appId, String key) {
        return builder()
                .tenant(tenant)
                .namespace(namespace)
                .appId(appId)
                .key(key)
                .build();
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

    public Builder toBuilder() {
        return builder()
                .tenant(tenant)
                .namespace(namespace)
                .appId(appId)
                .key(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigCoordinate that = (ConfigCoordinate) o;
        return tenant.equals(that.tenant)
                && namespace.equals(that.namespace)
                && appId.equals(that.appId)
                && key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenant, namespace, appId, key);
    }

    @Override
    public String toString() {
        return "ConfigCoordinate{" +
                "tenant='" + tenant + '\'' +
                ", namespace='" + namespace + '\'' +
                ", appId='" + appId + '\'' +
                ", key='" + key + '\'' +
                '}';
    }

    public static final class Builder {
        private String tenant;
        private String namespace;
        private String appId;
        private String key;

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

        public ConfigCoordinate build() {
            return new ConfigCoordinate(this);
        }
    }
}
