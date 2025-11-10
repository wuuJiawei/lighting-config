package io.lighting.config.core.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Authentication input produced by transport layers.
 */
public final class AuthRequest {

    private final String token;
    private final String tenant;
    private final String namespace;
    private final String appId;
    private final Map<String, String> attributes;

    private AuthRequest(Builder builder) {
        this.token = builder.token;
        this.tenant = builder.tenant;
        this.namespace = builder.namespace;
        this.appId = builder.appId;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(builder.attributes));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getToken() {
        return token;
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

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public static final class Builder {
        private String token = "";
        private String tenant = "default";
        private String namespace = "default";
        private String appId = "default";
        private Map<String, String> attributes = new LinkedHashMap<>();

        public Builder token(String token) {
            this.token = token;
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

        public Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(attributes);
            return this;
        }

        public AuthRequest build() {
            return new AuthRequest(this);
        }
    }
}
