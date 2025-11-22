package io.lighting.config.server.rest.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import static io.lighting.config.server.web.ApiConstants.DEFAULT_APP_ID;
import static io.lighting.config.server.web.ApiConstants.DEFAULT_NAMESPACE;
import static io.lighting.config.server.web.ApiConstants.DEFAULT_TENANT;

public class RollbackRequest {

    @NotBlank
    private String tenant = DEFAULT_TENANT;

    @NotBlank
    private String namespace = DEFAULT_NAMESPACE;

    @NotBlank
    private String appId = DEFAULT_APP_ID;

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
