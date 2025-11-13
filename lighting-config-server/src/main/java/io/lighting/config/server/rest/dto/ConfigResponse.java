package io.lighting.config.server.rest.dto;

import io.lighting.config.core.model.ConfigItem;

import java.time.Instant;
import java.util.Map;

public class ConfigResponse {

    private final String tenant;
    private final String namespace;
    private final String appId;
    private final String key;
    private final String value;
    private final String contentType;
    private final long version;
    private final Map<String, String> labels;
    private final boolean enabled;
    private final Instant updatedAt;
    private final String id;

    public ConfigResponse(ConfigItem item) {
        this.tenant = item.getTenant();
        this.namespace = item.getNamespace();
        this.appId = item.getAppId();
        this.key = item.getKey();
        this.value = item.getValue();
        this.contentType = item.getContentType().name();
        this.version = item.getVersion();
        this.labels = item.getLabels();
        this.enabled = item.isEnabled();
        this.updatedAt = item.getUpdatedAt();
        this.id = String.join(":", item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey());
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

    public String getValue() {
        return value;
    }

    public String getContentType() {
        return contentType;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getId() {
        return id;
    }
}
