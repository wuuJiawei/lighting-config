package io.lighting.config.server.rest.dto;

import java.time.Instant;

public class AuditRecordResponse {

    private final String id;
    private final String namespace;
    private final String appId;
    private final String configKey;
    private final String operator;
    private final String action;
    private final String message;
    private final Instant createdAt;

    public AuditRecordResponse(String id,
                               String namespace,
                               String appId,
                               String configKey,
                               String operator,
                               String action,
                               String message,
                               Instant createdAt) {
        this.id = id;
        this.namespace = namespace;
        this.appId = appId;
        this.configKey = configKey;
        this.operator = operator;
        this.action = action;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getAppId() {
        return appId;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getOperator() {
        return operator;
    }

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
