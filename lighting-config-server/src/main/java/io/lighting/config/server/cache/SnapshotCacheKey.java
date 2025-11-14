package io.lighting.config.server.cache;

import io.lighting.config.core.model.ConfigCoordinate;

import java.util.Objects;

public final class SnapshotCacheKey {

    private final String tenant;
    private final String namespace;
    private final String appId;
    private final String prefix;

    private SnapshotCacheKey(String tenant, String namespace, String appId, String prefix) {
        this.tenant = tenant;
        this.namespace = namespace;
        this.appId = appId;
        this.prefix = prefix == null ? "" : prefix;
    }

    static SnapshotCacheKey of(String tenant, String namespace, String appId, String prefix) {
        return new SnapshotCacheKey(tenant, namespace, appId, prefix);
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

    public String getPrefix() {
        return prefix;
    }

    boolean matches(ConfigCoordinate coordinate) {
        if (!tenant.equals(coordinate.getTenant())
                || !namespace.equals(coordinate.getNamespace())
                || !appId.equals(coordinate.getAppId())) {
            return false;
        }
        if (prefix.isEmpty()) {
            return true;
        }
        return coordinate.getKey().startsWith(prefix);
    }

    String selector() {
        return prefix.isEmpty() ? "*" : prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SnapshotCacheKey)) {
            return false;
        }
        SnapshotCacheKey that = (SnapshotCacheKey) o;
        return Objects.equals(tenant, that.tenant)
                && Objects.equals(namespace, that.namespace)
                && Objects.equals(appId, that.appId)
                && Objects.equals(prefix, that.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenant, namespace, appId, prefix);
    }
}
