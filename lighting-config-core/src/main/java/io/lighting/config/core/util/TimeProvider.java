package io.lighting.config.core.util;

import java.time.Clock;
import java.time.Instant;

/**
 * Abstraction for retrieving time (useful for testing and embedded scenarios).
 */
public interface TimeProvider {

    Instant now();

    static TimeProvider system() {
        return SystemTimeProvider.INSTANCE;
    }

    enum SystemTimeProvider implements TimeProvider {
        INSTANCE;

        private final Clock clock = Clock.systemUTC();

        @Override
        public Instant now() {
            return clock.instant();
        }
    }
}
