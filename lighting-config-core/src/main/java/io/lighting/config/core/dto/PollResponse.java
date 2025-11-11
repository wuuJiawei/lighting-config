package io.lighting.config.core.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of a polling request, containing config deltas or snapshot metadata.
 */
public final class PollResponse {

    private final long version;
    private final List<ConfigChange> items;
    private final PollAdvice advice;
    private final Instant serverTime;

    private PollResponse(Builder builder) {
        this.version = builder.version;
        this.items = Collections.unmodifiableList(List.copyOf(builder.items));
        this.advice = builder.advice == null ? PollAdvice.builder().build() : builder.advice;
        this.serverTime = builder.serverTime == null ? Instant.now() : builder.serverTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getVersion() {
        return version;
    }

    public List<ConfigChange> getItems() {
        return items;
    }

    public PollAdvice getAdvice() {
        return advice;
    }

    public Instant getServerTime() {
        return serverTime;
    }

    public boolean isUpToDate() {
        return items.isEmpty();
    }

    public static final class Builder {
        private long version;
        private List<ConfigChange> items = List.of();
        private PollAdvice advice;
        private Instant serverTime;

        public Builder version(long version) {
            this.version = version;
            return this;
        }

        public Builder items(List<ConfigChange> items) {
            this.items = items == null ? List.of() : List.copyOf(items);
            return this;
        }

        public Builder advice(PollAdvice advice) {
            this.advice = advice;
            return this;
        }

        public Builder serverTime(Instant serverTime) {
            this.serverTime = serverTime;
            return this;
        }

        public PollResponse build() {
            Objects.requireNonNull(items, "items");
            return new PollResponse(this);
        }
    }
}
