package io.lighting.config.core.dto;

import io.lighting.config.core.util.AppScope;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Describes a single polling call issued by the client to retrieve configuration changes.
 */
public final class PollRequest {

    private final String tenant;
    private final String namespace;
    private final String appId;
    private final Map<String, String> labels;
    private final Map<String, String> metadata;
    private final long lastVersion;
    private final List<String> prefixes;
    private final long clientTime;

    private PollRequest(Builder builder) {
        this.tenant = Objects.requireNonNull(builder.tenant, "tenant");
        this.namespace = Objects.requireNonNull(builder.namespace, "namespace");
        this.appId = Objects.requireNonNull(builder.appId, "appId");
        this.labels = Collections.unmodifiableMap(new LinkedHashMap<>(builder.labels));
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(builder.metadata));
        this.lastVersion = builder.lastVersion;
        this.prefixes = List.copyOf(builder.prefixes);
        this.clientTime = builder.clientTime == null ? System.currentTimeMillis() : builder.clientTime;
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

    public Map<String, String> getLabels() {
        return labels;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public long getLastVersion() {
        return lastVersion;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public long getClientTime() {
        return clientTime;
    }

    public List<String> getResolvedAppIds() {
        return AppScope.parseWithGlobal(appId);
    }

    public static final class Builder {
        private String tenant = "default";
        private String namespace = "default";
        private String appId = "default";
        private Map<String, String> labels = new LinkedHashMap<>();
        private Map<String, String> metadata = new LinkedHashMap<>();
        private long lastVersion;
        private List<String> prefixes = List.of();
        private Long clientTime;

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

        public Builder labels(Map<String, String> labels) {
            this.labels = labels == null ? new LinkedHashMap<>() : new LinkedHashMap<>(labels);
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
            return this;
        }

        public Builder lastVersion(long lastVersion) {
            this.lastVersion = lastVersion;
            return this;
        }

        public Builder prefixes(List<String> prefixes) {
            this.prefixes = prefixes == null ? List.of() : List.copyOf(prefixes);
            return this;
        }

        public Builder clientTime(Long clientTime) {
            this.clientTime = clientTime;
            return this;
        }

        public PollRequest build() {
            return new PollRequest(this);
        }
    }
}
