package io.lighting.config.core.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Generic audit log entry produced by security/ops sensitive operations.
 */
public final class AuditLog {

    private final String id;
    private final String tenant;
    private final String namespace;
    private final String appId;
    private final String action;
    private final String operator;
    private final String details;
    private final Instant createdAt;

    private AuditLog(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.tenant = Objects.requireNonNull(builder.tenant, "tenant");
        this.namespace = builder.namespace;
        this.appId = builder.appId;
        this.action = Objects.requireNonNull(builder.action, "action");
        this.operator = Objects.requireNonNull(builder.operator, "operator");
        this.details = builder.details;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
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

    public String getAction() {
        return action;
    }

    public String getOperator() {
        return operator;
    }

    public String getDetails() {
        return details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private String id = "";
        private String tenant = "default";
        private String namespace = "default";
        private String appId = "default";
        private String action = "";
        private String operator = "system";
        private String details = "";
        private Instant createdAt = Instant.EPOCH;

        public Builder id(String id) {
            this.id = id;
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

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(this);
        }
    }
}
