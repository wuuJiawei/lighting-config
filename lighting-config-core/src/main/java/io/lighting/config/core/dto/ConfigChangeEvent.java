package io.lighting.config.core.dto;

import io.lighting.config.core.model.ConfigCoordinate;

import java.time.Instant;
import java.util.Objects;

/**
 * Event emitted inside the server whenever a configuration value changes.
 */
public final class ConfigChangeEvent {

    private final ConfigChange change;
    private final String operator;
    private final String source;
    private final Instant publishedAt;

    private ConfigChangeEvent(Builder builder) {
        this.change = Objects.requireNonNull(builder.change, "change");
        this.operator = builder.operator;
        this.source = builder.source;
        this.publishedAt = Objects.requireNonNull(builder.publishedAt, "publishedAt");
    }

    public static Builder builder() {
        return new Builder();
    }

    public ConfigChange getChange() {
        return change;
    }

    public ConfigCoordinate getCoordinate() {
        return change.getCoordinate();
    }

    public String getOperator() {
        return operator;
    }

    public String getSource() {
        return source;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public static final class Builder {
        private ConfigChange change;
        private String operator = "system";
        private String source = "server";
        private Instant publishedAt = Instant.EPOCH;

        public Builder change(ConfigChange change) {
            this.change = change;
            return this;
        }

        public Builder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder publishedAt(Instant publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public ConfigChangeEvent build() {
            return new ConfigChangeEvent(this);
        }
    }
}
