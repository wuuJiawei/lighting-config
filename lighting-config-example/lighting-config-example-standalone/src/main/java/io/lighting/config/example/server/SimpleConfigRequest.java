package io.lighting.config.example.server;

import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ContentType;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DTO used by {@link SimpleConfigController} to convert HTTP payloads to {@link ConfigItem}.
 */
public class SimpleConfigRequest {

    private String tenant = "default";
    private String namespace = "default";
    private String appId = "demo-server";
    private String key;
    private String value;
    private String contentType = ContentType.STRING.name();
    private Map<String, String> labels = new LinkedHashMap<>();
    private boolean enabled = true;

    public ConfigItem toConfigItem(Instant timestamp) {
        Assert.hasText(key, "key must not be empty");
        Assert.hasText(value, "value must not be empty");
        return ConfigItem.builder()
                .tenant(tenant)
                .namespace(namespace)
                .appId(appId)
                .key(key)
                .contentType(ContentType.fromAlias(contentType))
                .value(value)
                .labels(labels)
                .enabled(enabled)
                .version(0L)
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels == null ? new LinkedHashMap<>() : new LinkedHashMap<>(labels);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
