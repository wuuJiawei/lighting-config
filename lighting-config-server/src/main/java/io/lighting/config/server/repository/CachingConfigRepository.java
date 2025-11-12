package io.lighting.config.server.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ConfigItem;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Decorator that adds a local Caffeine cache around the configured {@link ConfigRepository}.
 * Ensures snapshot reads hit the cache first while writes invalidate affected entries.
 */
public class CachingConfigRepository implements ConfigRepository {

    private final ConfigRepository delegate;
    private final Cache<ConfigCoordinate, Optional<ConfigItem>> itemCache;
    private final Cache<ListKey, List<ConfigItem>> listCache;

    public CachingConfigRepository(ConfigRepository delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.itemCache = Caffeine.newBuilder()
                .maximumSize(50_000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
        this.listCache = Caffeine.newBuilder()
                .maximumSize(5_000)
                .expireAfterWrite(Duration.ofMinutes(2))
                .build();
    }

    @Override
    public Optional<ConfigItem> get(String tenant, String namespace, String appId, String key) {
        ConfigCoordinate coordinate = ConfigCoordinate.of(tenant, namespace, appId, key);
        Optional<ConfigItem> cached = itemCache.get(coordinate,
                c -> delegate.get(c.getTenant(), c.getNamespace(), c.getAppId(), c.getKey()));
        return cached == null ? Optional.empty() : cached;
    }

    @Override
    public List<ConfigItem> list(String tenant, String namespace, String appId, String prefix) {
        ListKey key = new ListKey(tenant, namespace, appId, prefix);
        return listCache.get(key, k -> List.copyOf(delegate.list(
                k.tenant, k.namespace, k.appId, k.prefix)));
    }

    @Override
    public void upsert(ConfigItem item, String operator) {
        delegate.upsert(item, operator);
        invalidate(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey());
    }

    @Override
    public void delete(String tenant, String namespace, String appId, String key, String operator) {
        delegate.delete(tenant, namespace, appId, key, operator);
        invalidate(tenant, namespace, appId, key);
    }

    @Override
    public long currentVersion(String tenant, String namespace, String appId, String key) {
        return get(tenant, namespace, appId, key)
                .map(ConfigItem::getVersion)
                .orElse(0L);
    }

    private void invalidate(String tenant, String namespace, String appId, String key) {
        itemCache.invalidate(ConfigCoordinate.of(tenant, namespace, appId, key));
        listCache.asMap().keySet().stream()
                .filter(k -> k.matches(tenant, namespace, appId, key))
                .collect(Collectors.toList())
                .forEach(listCache::invalidate);
    }

    private static final class ListKey {
        private final String tenant;
        private final String namespace;
        private final String appId;
        private final String prefix;

        private ListKey(String tenant, String namespace, String appId, String prefix) {
            this.tenant = tenant;
            this.namespace = namespace;
            this.appId = appId;
            this.prefix = prefix;
        }

        private boolean matches(String tenant, String namespace, String appId, String key) {
            if (!this.tenant.equals(tenant) || !this.namespace.equals(namespace) || !this.appId.equals(appId)) {
                return false;
            }
            if (prefix == null || prefix.isEmpty()) {
                return true;
            }
            return key.startsWith(prefix);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ListKey)) {
                return false;
            }
            ListKey listKey = (ListKey) o;
            return Objects.equals(tenant, listKey.tenant)
                    && Objects.equals(namespace, listKey.namespace)
                    && Objects.equals(appId, listKey.appId)
                    && Objects.equals(prefix, listKey.prefix);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenant, namespace, appId, prefix);
        }
    }
}
