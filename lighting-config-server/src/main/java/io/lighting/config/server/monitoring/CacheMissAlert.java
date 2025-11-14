package io.lighting.config.server.monitoring;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a persisted cache miss burst that should surface on the console for visibility.
 */
public class CacheMissAlert {

    private final String tenant;
    private final String namespace;
    private final String appId;
    private final String selector;
    private final int missCount;
    private final Instant occurredAt;

    private CacheMissAlert(Builder builder) {
        this.tenant = builder.tenant;
        this.namespace = builder.namespace;
        this.appId = builder.appId;
        this.selector = builder.selector;
        this.missCount = builder.missCount;
        this.occurredAt = builder.occurredAt;
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

    public String getSelector() {
        return selector;
    }

    public int getMissCount() {
        return missCount;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public static final class Builder {
        private String tenant;
        private String namespace;
        private String appId;
        private String selector;
        private int missCount;
        private Instant occurredAt = Instant.now();

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

        public Builder selector(String selector) {
            this.selector = selector;
            return this;
        }

        public Builder missCount(int missCount) {
            this.missCount = missCount;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public CacheMissAlert build() {
            Objects.requireNonNull(tenant, "tenant");
            Objects.requireNonNull(namespace, "namespace");
            Objects.requireNonNull(appId, "appId");
            Objects.requireNonNull(selector, "selector");
            Objects.requireNonNull(occurredAt, "occurredAt");
            return new CacheMissAlert(this);
        }
    }
}
