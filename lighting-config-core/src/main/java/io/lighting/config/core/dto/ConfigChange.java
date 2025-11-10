package io.lighting.config.core.dto;

import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ContentType;

import java.time.Instant;
import java.util.Objects;

/**
 * Lightweight DTO pushed through the event bus / gRPC watch channel.
 */
public final class ConfigChange {

    private final ConfigCoordinate coordinate;
    private final long version;
    private final ChangeType type;
    private final ContentType contentType;
    private final String value;
    private final boolean deleted;
    private final Instant occurredAt;

    private ConfigChange(Builder builder) {
        this.coordinate = Objects.requireNonNull(builder.coordinate, "coordinate");
        this.version = builder.version;
        this.type = Objects.requireNonNull(builder.type, "type");
        this.contentType = Objects.requireNonNull(builder.contentType, "contentType");
        this.value = builder.value;
        this.deleted = builder.deleted;
        this.occurredAt = Objects.requireNonNull(builder.occurredAt, "occurredAt");
    }

    public static Builder builder() {
        return new Builder();
    }

    public ConfigCoordinate getCoordinate() {
        return coordinate;
    }

    public long getVersion() {
        return version;
    }

    public ChangeType getType() {
        return type;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getValue() {
        return value;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public static final class Builder {
        private ConfigCoordinate coordinate;
        private long version;
        private ChangeType type = ChangeType.UPSERT;
        private ContentType contentType = ContentType.TEXT;
        private String value = "";
        private boolean deleted;
        private Instant occurredAt = Instant.EPOCH;

        public Builder coordinate(ConfigCoordinate coordinate) {
            this.coordinate = coordinate;
            return this;
        }

        public Builder version(long version) {
            this.version = version;
            return this;
        }

        public Builder type(ChangeType type) {
            this.type = type;
            return this;
        }

        public Builder contentType(ContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public ConfigChange build() {
            return new ConfigChange(this);
        }
    }
}
