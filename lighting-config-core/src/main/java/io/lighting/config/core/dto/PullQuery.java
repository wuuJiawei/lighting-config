package io.lighting.config.core.dto;

import java.util.Objects;

/**
 * Describes a pull request resolved before hitting the transport layer.
 */
public final class PullQuery {

    private final String tenant;
    private final String namespace;
    private final String appId;
    private final ConfigSelector selector;

    private PullQuery(Builder builder) {
        this.tenant = Objects.requireNonNull(builder.tenant, "tenant");
        this.namespace = Objects.requireNonNull(builder.namespace, "namespace");
        this.appId = Objects.requireNonNull(builder.appId, "appId");
        this.selector = builder.selector == null ? ConfigSelector.wildcard() : builder.selector;
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

    public ConfigSelector getSelector() {
        return selector;
    }

    public static final class Builder {
        private String tenant = "default";
        private String namespace = "default";
        private String appId = "default";
        private ConfigSelector selector = ConfigSelector.wildcard();

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

        public Builder selector(ConfigSelector selector) {
            this.selector = selector;
            return this;
        }

        public PullQuery build() {
            return new PullQuery(this);
        }
    }
}
