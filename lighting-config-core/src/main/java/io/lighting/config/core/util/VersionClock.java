package io.lighting.config.core.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple monotonic clock that can be shared when DB-generated versions are unavailable (embedded mode).
 */
public final class VersionClock {

    private final AtomicLong counter;

    public VersionClock(long initialValue) {
        this.counter = new AtomicLong(initialValue);
    }

    public long next() {
        return counter.incrementAndGet();
    }

    public long current() {
        return counter.get();
    }
}
