package io.lighting.config.client.config;

import io.lighting.config.core.util.AppScope;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable configuration used by {@code LightingClient}.
 */
public final class ClientOptions {

    private final String serverAddress;
    private final boolean useTls;
    private final String tenant;
    private final String namespace;
    private final String appId;
    private final List<String> resolvedAppIds;
    private final Map<String, String> labels;
    private final Map<String, String> metadata;
    private final List<String> bootstrapPrefixes;
    private final Duration pollInterval;
    private final boolean bannerEnabled;
    private final String authToken;

    private ClientOptions(Builder builder) {
        this.serverAddress = Objects.requireNonNull(builder.serverAddress, "serverAddress");
        this.useTls = builder.useTls;
        this.tenant = Objects.requireNonNull(builder.tenant, "tenant");
        this.namespace = Objects.requireNonNull(builder.namespace, "namespace");
        this.appId = Objects.requireNonNull(builder.appId, "appId");
        this.labels = Collections.unmodifiableMap(new LinkedHashMap<>(builder.labels));
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(builder.metadata));
        this.bootstrapPrefixes = List.copyOf(builder.bootstrapPrefixes);
        this.pollInterval = builder.pollInterval;
        this.bannerEnabled = builder.bannerEnabled;
        this.authToken = builder.authToken;
        this.resolvedAppIds = AppScope.parseWithGlobal(this.appId);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public boolean isUseTls() {
        return useTls;
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

    public List<String> getResolvedAppIds() {
        return resolvedAppIds;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public List<String> getBootstrapPrefixes() {
        return bootstrapPrefixes;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public boolean isBannerEnabled() {
        return bannerEnabled;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Builder toBuilder() {
        return new Builder()
                .serverAddress(serverAddress)
                .useTls(useTls)
                .tenant(tenant)
                .namespace(namespace)
                .appId(appId)
                .authToken(authToken)
                .labels(labels)
                .metadata(metadata)
                .bootstrapPrefixes(bootstrapPrefixes)
                .pollInterval(pollInterval)
                .bannerEnabled(bannerEnabled);
    }

    public static final class Builder {
        private String serverAddress = "http://localhost:7086";
        private boolean useTls = false;
        private String tenant = "default";
        private String namespace = "default";
        private String appId = "default";
        private Map<String, String> labels = new LinkedHashMap<>();
        private Map<String, String> metadata = new LinkedHashMap<>();
        private List<String> bootstrapPrefixes = List.of();
        private Duration pollInterval = Duration.ofSeconds(30);
        private boolean bannerEnabled = true;
        private String authToken;

        public Builder serverAddress(String serverAddress) {
            this.serverAddress = serverAddress;
            return this;
        }

        public Builder useTls(boolean useTls) {
            this.useTls = useTls;
            return this;
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

        public Builder labels(Map<String, String> labels) {
            this.labels = labels == null ? new LinkedHashMap<>() : new LinkedHashMap<>(labels);
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
            return this;
        }

        public Builder addLabel(String key, String value) {
            this.labels.put(key, value);
            return this;
        }

        public Builder addMetadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        public Builder bootstrapPrefixes(List<String> prefixes) {
            this.bootstrapPrefixes = prefixes == null ? List.of() : List.copyOf(prefixes);
            return this;
        }

        public Builder pollInterval(Duration duration) {
            this.pollInterval = duration == null ? Duration.ofSeconds(30) : duration;
            return this;
        }

        public Builder bannerEnabled(boolean bannerEnabled) {
            this.bannerEnabled = bannerEnabled;
            return this;
        }

        public Builder authToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public ClientOptions build() {
            return new ClientOptions(this);
        }
    }
}
