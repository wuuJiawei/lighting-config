package io.lighting.config.server.rest.dto;

import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ContentType;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigUpsertRequest {

    @NotBlank
    private String tenant = "default";
    @NotBlank
    private String namespace = "default";
    @NotBlank
    private String appId = "default";
    @NotBlank
    private String key;
    @NotBlank
    private String value;
    private String contentType = ContentType.STRING.name();
    private Map<String, String> labels = new LinkedHashMap<>();
    private boolean enabled = true;

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
        this.labels = labels;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ConfigItem toConfigItem(Instant timestamp) {
        return ConfigItem.builder()
                .tenant(tenant)
                .namespace(namespace)
                .appId(appId)
                .key(key)
                .value(value)
                .contentType(ContentType.fromAlias(contentType))
                .labels(labels)
                .enabled(enabled)
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
    }
}
