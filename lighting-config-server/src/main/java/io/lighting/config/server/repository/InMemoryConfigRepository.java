package io.lighting.config.server.repository;

import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.core.model.ConfigItem;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryConfigRepository implements ConfigRepository {

    private final Map<String, ConfigItem> store = new ConcurrentHashMap<>();
    private final AtomicLong versionGenerator = new AtomicLong(0);

    @Override
    public Optional<ConfigItem> get(String tenant, String namespace, String appId, String key) {
        return Optional.ofNullable(store.get(k(tenant, namespace, appId, key)));
    }

    @Override
    public List<ConfigItem> list(String tenant, String namespace, String appId, String prefix) {
        return store.values().stream()
                .filter(item -> item.getTenant().equals(tenant)
                        && item.getNamespace().equals(namespace)
                        && item.getAppId().equals(appId)
                        && (prefix == null || item.getKey().startsWith(prefix)))
                .sorted(Comparator.comparing(ConfigItem::getKey))
                .collect(Collectors.toList());
    }

    @Override
    public void upsert(ConfigItem item, String operator) {
        long version = versionGenerator.incrementAndGet();
        Instant now = Instant.now();
        Instant createdAt = item.getCreatedAt().equals(Instant.EPOCH) ? now : item.getCreatedAt();
        ConfigItem persisted = item.toBuilder()
                .version(version)
                .createdAt(createdAt)
                .updatedAt(now)
                .build();
        store.put(k(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey()), persisted);
    }

    @Override
    public void delete(String tenant, String namespace, String appId, String key, String operator) {
        store.remove(k(tenant, namespace, appId, key));
    }

    @Override
    public long currentVersion(String tenant, String namespace, String appId, String key) {
        return get(tenant, namespace, appId, key)
                .map(ConfigItem::getVersion)
                .orElse(0L);
    }

    private String k(String tenant, String namespace, String appId, String key) {
        return tenant + "|" + namespace + "|" + appId + "|" + key;
    }
}
