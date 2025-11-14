package io.lighting.config.server.cache;

import io.lighting.config.core.util.TimeProvider;
import io.lighting.config.server.monitoring.CacheMissAlert;
import io.lighting.config.server.monitoring.CacheMissAlertRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheMissTracker {

    private final CacheMissAlertRepository repository;
    private final int threshold;
    private final TimeProvider timeProvider;
    private final Map<SnapshotCacheKey, Integer> counters = new ConcurrentHashMap<>();

    public CacheMissTracker(CacheMissAlertRepository repository, int threshold, TimeProvider timeProvider) {
        this.repository = repository;
        this.threshold = threshold;
        this.timeProvider = timeProvider;
    }

    public void recordMiss(SnapshotCacheKey key) {
        int next = counters.compute(key, (k, v) -> v == null ? 1 : v + 1);
        if (next >= threshold) {
            CacheMissAlert alert = CacheMissAlert.builder()
                    .tenant(key.getTenant())
                    .namespace(key.getNamespace())
                    .appId(key.getAppId())
                    .selector(key.selector())
                    .missCount(next)
                    .occurredAt(timeProvider.now())
                    .build();
            repository.save(alert);
            counters.put(key, 0);
        }
    }

    public void reset(SnapshotCacheKey key) {
        counters.remove(key);
    }
}
