package io.lighting.config.server.rest.dto;

import io.lighting.config.core.dto.PollRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PollRequestPayload {

    private String tenant = "default";
    private String namespace = "default";
    private String appId = "default";
    private Map<String, String> labels = new LinkedHashMap<>();
    private Map<String, String> metadata = new LinkedHashMap<>();
    private long lastVersion;
    private List<String> prefixes = List.of();
    private Long clientTime;

    public PollRequest toRequest() {
        return PollRequest.builder()
                .tenant(tenant)
                .namespace(namespace)
                .appId(appId)
                .labels(labels)
                .metadata(metadata)
                .lastVersion(lastVersion)
                .prefixes(prefixes)
                .clientTime(clientTime != null ? clientTime : System.currentTimeMillis())
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

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels == null ? new LinkedHashMap<>() : new LinkedHashMap<>(labels);
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
    }

    public long getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(long lastVersion) {
        this.lastVersion = lastVersion;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes == null ? List.of() : List.copyOf(prefixes);
    }

    public Long getClientTime() {
        return clientTime;
    }

    public void setClientTime(Long clientTime) {
        this.clientTime = clientTime;
    }
}
