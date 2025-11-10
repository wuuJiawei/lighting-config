package io.lighting.config.core.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents an environment or logical boundary for configurations.
 */
public final class Namespace {

    private final String tenant;
    private final String code;
    private final String displayName;
    private final String description;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Namespace(Builder builder) {
        this.tenant = Objects.requireNonNull(builder.tenant, "tenant");
        this.code = Objects.requireNonNull(builder.code, "code");
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(builder.updatedAt, "updatedAt");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTenant() {
        return tenant;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Builder toBuilder() {
        return builder()
                .tenant(tenant)
                .code(code)
                .displayName(displayName)
                .description(description)
                .createdAt(createdAt)
                .updatedAt(updatedAt);
    }

    public static final class Builder {
        private String tenant;
        private String code;
        private String displayName = "";
        private String description = "";
        private Instant createdAt = Instant.EPOCH;
        private Instant updatedAt = Instant.EPOCH;

        private Builder() {
        }

        public Builder tenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Namespace build() {
            return new Namespace(this);
        }
    }
}
