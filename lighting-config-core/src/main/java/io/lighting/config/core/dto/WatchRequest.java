package io.lighting.config.core.dto;

import io.lighting.config.core.model.LabelSet;

import java.util.Objects;

/**
 * Describes a streaming subscription request before it becomes a gRPC message.
 */
public final class WatchRequest {

    private final String tenant;
    private final String namespace;
    private final String appId;
    private final LabelSet labels;
    private final ClientMetadata client;
    private final long lastKnownVersion;

    private WatchRequest(Builder builder) {
        this.tenant = Objects.requireNonNull(builder.tenant, "tenant");
        this.namespace = Objects.requireNonNull(builder.namespace, "namespace");
        this.appId = Objects.requireNonNull(builder.appId, "appId");
        this.labels = builder.labels == null ? LabelSet.empty() : builder.labels;
        this.client = Objects.requireNonNull(builder.client, "client");
        this.lastKnownVersion = builder.lastKnownVersion;
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

    public LabelSet getLabels() {
        return labels;
    }

    public ClientMetadata getClient() {
        return client;
    }

    public long getLastKnownVersion() {
        return lastKnownVersion;
    }

    public static final class Builder {
        private String tenant = "default";
        private String namespace = "default";
        private String appId = "default";
        private LabelSet labels = LabelSet.empty();
        private ClientMetadata client = ClientMetadata.builder().build();
        private long lastKnownVersion;

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

        public Builder labels(LabelSet labels) {
            this.labels = labels;
            return this;
        }

        public Builder client(ClientMetadata client) {
            this.client = client;
            return this;
        }

        public Builder lastKnownVersion(long lastKnownVersion) {
            this.lastKnownVersion = lastKnownVersion;
            return this;
        }

        public WatchRequest build() {
            return new WatchRequest(this);
        }
    }
}
