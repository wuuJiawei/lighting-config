package io.lighting.config.core.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Describes a client instance watching or pulling configurations.
 */
public final class ClientMetadata {

    private final String clientId;
    private final String hostname;
    private final String ip;
    private final Map<String, String> attributes;

    private ClientMetadata(Builder builder) {
        this.clientId = Objects.requireNonNull(builder.clientId, "clientId");
        this.hostname = builder.hostname;
        this.ip = builder.ip;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(builder.attributes));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getClientId() {
        return clientId;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public static final class Builder {
        private String clientId = "unknown";
        private String hostname = "";
        private String ip = "";
        private Map<String, String> attributes = new LinkedHashMap<>();

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(attributes);
            return this;
        }

        public ClientMetadata build() {
            return new ClientMetadata(this);
        }
    }
}
