package io.lighting.config.server.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PollRequest;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ConfigItem;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serves client snapshots by checking a local cache before touching the database.
 */
public class ClientSnapshotService {

    private final ConfigRepository repository;
    private final CacheMissTracker missTracker;
    private final Cache<SnapshotCacheKey, List<ConfigItem>> snapshotCache;

    public ClientSnapshotService(ConfigRepository repository,
                                 CacheMissTracker missTracker) {
        this.repository = repository;
        this.missTracker = missTracker;
        this.snapshotCache = Caffeine.newBuilder()
                .maximumSize(5_000)
                .expireAfterWrite(Duration.ofMinutes(3))
                .build();
    }

    public List<ConfigChange> snapshot(PollRequest request) {
        Map<String, ConfigChange> merged = new LinkedHashMap<>();
        List<String> prefixes = request.getPrefixes().isEmpty() ? List.of("") : request.getPrefixes();
        for (String appId : request.getResolvedAppIds()) {
            for (String prefix : prefixes) {
                List<ConfigItem> items = fetchItems(request.getTenant(), request.getNamespace(), appId, prefix);
                items.stream()
                        .map(this::toChange)
                        .forEach(change -> merged.put(change.getCoordinate().getKey(), change));
            }
        }
        return new ArrayList<>(merged.values());
    }

    public void invalidate(ConfigCoordinate coordinate) {
        snapshotCache.asMap().keySet().stream()
                .filter(key -> key.matches(coordinate))
                .collect(Collectors.toList())
                .forEach(snapshotCache::invalidate);
    }

    private List<ConfigItem> fetchItems(String tenant, String namespace, String appId, String rawPrefix) {
        String prefix = normalize(rawPrefix);
        SnapshotCacheKey cacheKey = SnapshotCacheKey.of(tenant, namespace, appId, prefix);
        List<ConfigItem> cached = snapshotCache.getIfPresent(cacheKey);
        if (cached != null) {
            missTracker.reset(cacheKey);
            return cached;
        }
        List<ConfigItem> loaded = repository.list(tenant, namespace, appId, prefix.isEmpty() ? null : prefix);
        snapshotCache.put(cacheKey, loaded);
        missTracker.recordMiss(cacheKey);
        return loaded;
    }

    private ConfigChange toChange(ConfigItem item) {
        return ConfigChange.builder()
                .coordinate(ConfigCoordinate.of(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey()))
                .version(item.getVersion())
                .type(item.isEnabled() ? ChangeType.UPSERT : ChangeType.DELETE)
                .contentType(item.getContentType())
                .value(item.getValue())
                .deleted(!item.isEnabled())
                .occurredAt(item.getUpdatedAt().toEpochMilli())
                .build();
    }

    private String normalize(String prefix) {
        return prefix == null ? "" : prefix.trim();
    }
}
