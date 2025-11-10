package io.lighting.config.core.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Historical snapshot produced whenever a configuration item mutates.
 */
public final class Revision {

    private final ConfigCoordinate coordinate;
    private final long version;
    private final RevisionOperation operation;
    private final String operator;
    private final String diff;
    private final Instant createdAt;

    private Revision(Builder builder) {
        this.coordinate = Objects.requireNonNull(builder.coordinate, "coordinate");
        this.version = builder.version;
        this.operation = Objects.requireNonNull(builder.operation, "operation");
        this.operator = Objects.requireNonNull(builder.operator, "operator");
        this.diff = builder.diff;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt");
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

    public RevisionOperation getOperation() {
        return operation;
    }

    public String getOperator() {
        return operator;
    }

    public String getDiff() {
        return diff;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private ConfigCoordinate coordinate;
        private long version;
        private RevisionOperation operation = RevisionOperation.UPSERT;
        private String operator = "system";
        private String diff = "";
        private Instant createdAt = Instant.EPOCH;

        public Builder coordinate(ConfigCoordinate coordinate) {
            this.coordinate = coordinate;
            return this;
        }

        public Builder version(long version) {
            this.version = version;
            return this;
        }

        public Builder operation(RevisionOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public Builder diff(String diff) {
            this.diff = diff;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Revision build() {
            return new Revision(this);
        }
    }
}
