package io.lighting.config.core.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Logical application that consumes configurations.
 */
public final class App {

    private final String tenant;
    private final String appId;
    private final String displayName;
    private final Set<String> owners;
    private final String description;
    private final Instant createdAt;
    private final Instant updatedAt;

    private App(Builder builder) {
        this.tenant = Objects.requireNonNull(builder.tenant, "tenant");
        this.appId = Objects.requireNonNull(builder.appId, "appId");
        this.displayName = builder.displayName;
        this.owners = Collections.unmodifiableSet(new LinkedHashSet<>(builder.owners));
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

    public String getAppId() {
        return appId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<String> getOwners() {
        return owners;
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
                .appId(appId)
                .displayName(displayName)
                .owners(owners)
                .description(description)
                .createdAt(createdAt)
                .updatedAt(updatedAt);
    }

    public static final class Builder {
        private String tenant;
        private String appId;
        private String displayName = "";
        private Set<String> owners = new LinkedHashSet<>();
        private String description = "";
        private Instant createdAt = Instant.EPOCH;
        private Instant updatedAt = Instant.EPOCH;

        private Builder() {
        }

        public Builder tenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder owners(Set<String> owners) {
            this.owners = owners == null ? new LinkedHashSet<>() : new LinkedHashSet<>(owners);
            return this;
        }

        public Builder addOwner(String owner) {
            this.owners.add(owner);
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

        public App build() {
            return new App(this);
        }
    }
}
