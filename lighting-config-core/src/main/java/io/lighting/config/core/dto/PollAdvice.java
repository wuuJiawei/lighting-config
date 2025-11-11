package io.lighting.config.core.dto;

import java.time.Duration;
import java.util.Objects;

/**
 * Guidance returned alongside a {@link PollResponse} describing how the client
 * should schedule the next poll request.
 */
public final class PollAdvice {

    private final Duration nextInterval;
    private final boolean throttled;

    private PollAdvice(Builder builder) {
        this.nextInterval = Objects.requireNonNull(builder.nextInterval, "nextInterval");
        this.throttled = builder.throttled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Duration getNextInterval() {
        return nextInterval;
    }

    public boolean isThrottled() {
        return throttled;
    }

    public static final class Builder {
        private Duration nextInterval = Duration.ofSeconds(30);
        private boolean throttled;

        public Builder nextInterval(Duration nextInterval) {
            this.nextInterval = nextInterval;
            return this;
        }

        public Builder throttled(boolean throttled) {
            this.throttled = throttled;
            return this;
        }

        public PollAdvice build() {
            return new PollAdvice(this);
        }
    }
}
