package io.lighting.config.server.rest.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class RollbackRequest {

    @NotBlank
    private String tenant = "default";

    @NotBlank
    private String namespace = "default";

    @NotBlank
    private String appId = "default";

    @NotBlank
    private String key;

    @Min(1)
    private long targetVersion;

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

    public long getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(long targetVersion) {
        this.targetVersion = targetVersion;
    }
}
