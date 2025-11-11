package io.lighting.config.example.server;

import io.lighting.config.core.dto.ConfigSelector;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.util.TimeProvider;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation that backs the example server.
 */
public class InMemoryConfigApplicationService implements ConfigApplicationService {

    private final ConcurrentMap<String, ConfigItem> store = new ConcurrentHashMap<>();
    private final TimeProvider timeProvider;

    public InMemoryConfigApplicationService(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public Optional<ConfigItem> get(String tenant, String namespace, String appId, String key) {
        return Optional.ofNullable(store.get(coord(tenant, namespace, appId, key)));
    }

    @Override
    public List<ConfigItem> list(PullQuery query) {
        return store.values().stream()
                .filter(item -> matches(query, item))
                .sorted(Comparator.comparing(ConfigItem::getKey))
                .collect(Collectors.toList());
    }

    @Override
    public ConfigItem upsert(ConfigItem item, String operator) {
        final String coordinate = coord(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey());
        return store.compute(coordinate, (key, existing) -> {
            Instant now = timeProvider.now();
            long nextVersion = existing == null ? 1L : existing.getVersion() + 1;
            Instant createdAt = existing == null ? now : existing.getCreatedAt();
            return ConfigItem.builder()
                    .tenant(item.getTenant())
                    .namespace(item.getNamespace())
                    .appId(item.getAppId())
                    .key(item.getKey())
                    .contentType(item.getContentType())
                    .value(item.getValue())
                    .labels(item.getLabels())
                    .enabled(item.isEnabled())
                    .version(nextVersion)
                    .createdAt(createdAt)
                    .updatedAt(now)
                    .build();
        });
    }

    @Override
    public void delete(String tenant, String namespace, String appId, String key, String operator) {
        store.remove(coord(tenant, namespace, appId, key));
    }

    private boolean matches(PullQuery query, ConfigItem item) {
        if (!item.getTenant().equals(query.getTenant())) {
            return false;
        }
        if (!item.getNamespace().equals(query.getNamespace())) {
            return false;
        }
        if (!item.getAppId().equals(query.getAppId())) {
            return false;
        }
        return matchesSelector(query.getSelector(), item.getKey());
    }

    private boolean matchesSelector(ConfigSelector selector, String key) {
        if (selector == null || selector.isWildcard()) {
            return true;
        }
        return selector.getKey()
                .map(key::equals)
                .or(() -> selector.getPrefix().map(key::startsWith))
                .orElse(true);
    }

    private String coord(String tenant, String namespace, String appId, String key) {
        return String.join("|", tenant, namespace, appId, key);
    }
}
