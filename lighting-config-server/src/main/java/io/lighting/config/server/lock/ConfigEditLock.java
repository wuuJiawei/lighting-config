package io.lighting.config.server.lock;

import io.lighting.config.core.model.ConfigCoordinate;

import java.time.Instant;
import java.util.Objects;

public class ConfigEditLock {

    private final ConfigCoordinate coordinate;
    private final String ownerId;
    private final String ownerName;
    private final Instant expiresAt;
    private final Instant updatedAt;

    private ConfigEditLock(Builder builder) {
        this.coordinate = builder.coordinate;
        this.ownerId = builder.ownerId;
        this.ownerName = builder.ownerName;
        this.expiresAt = builder.expiresAt;
        this.updatedAt = builder.updatedAt;
    }

    public ConfigCoordinate getCoordinate() {
        return coordinate;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isOwnedBy(String candidate) {
        return candidate != null && candidate.equals(ownerId);
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public String ownerFingerprint() {
        if (ownerId == null) {
            return "";
        }
        return ownerId.length() <= 8 ? ownerId : ownerId.substring(0, 8);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ConfigCoordinate coordinate;
        private String ownerId;
        private String ownerName;
        private Instant expiresAt;
        private Instant updatedAt;

        public Builder coordinate(ConfigCoordinate coordinate) {
            this.coordinate = coordinate;
            return this;
        }

        public Builder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder ownerName(String ownerName) {
            this.ownerName = ownerName;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ConfigEditLock build() {
            Objects.requireNonNull(coordinate, "coordinate must not be null");
            Objects.requireNonNull(ownerId, "ownerId must not be null");
            Objects.requireNonNull(expiresAt, "expiresAt must not be null");
            Objects.requireNonNull(updatedAt, "updatedAt must not be null");
            if (ownerName == null) {
                ownerName = "";
            }
            return new ConfigEditLock(this);
        }
    }
}
