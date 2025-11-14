package io.lighting.config.server.monitoring;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class InMemoryCacheMissAlertRepository implements CacheMissAlertRepository {

    private final CopyOnWriteArrayList<CacheMissAlert> events = new CopyOnWriteArrayList<>();
    private final int maxSize;

    public InMemoryCacheMissAlertRepository() {
        this(200);
    }

    public InMemoryCacheMissAlertRepository(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void save(CacheMissAlert alert) {
        events.add(alert);
        while (events.size() > maxSize) {
            events.remove(0);
        }
    }

    @Override
    public List<CacheMissAlert> findRecent(String tenant, int limit) {
        return events.stream()
                .filter(alert -> alert.getTenant().equals(tenant))
                .sorted((a, b) -> b.getOccurredAt().compareTo(a.getOccurredAt()))
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void reset() {
        events.clear();
    }
}
