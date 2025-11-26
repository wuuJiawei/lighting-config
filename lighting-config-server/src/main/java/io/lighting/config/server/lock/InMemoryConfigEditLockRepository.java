package io.lighting.config.server.lock;

import io.lighting.config.core.model.ConfigCoordinate;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryConfigEditLockRepository implements ConfigEditLockRepository {

    private final Map<String, ConfigEditLock> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<ConfigEditLock> find(ConfigCoordinate coordinate) {
        return Optional.ofNullable(storage.get(key(coordinate)));
    }

    @Override
    public void upsert(ConfigEditLock lock) {
        storage.put(key(lock.getCoordinate()), lock);
    }

    @Override
    public boolean deleteIfOwned(ConfigCoordinate coordinate, String ownerId) {
        String key = key(coordinate);
        ConfigEditLock existing = storage.get(key);
        if (existing != null && existing.isOwnedBy(ownerId)) {
            storage.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public Optional<ConfigEditLock> deleteIfExpired(ConfigCoordinate coordinate, Instant now) {
        String key = key(coordinate);
        ConfigEditLock existing = storage.get(key);
        if (existing != null && existing.isExpired(now)) {
            storage.remove(key);
            return Optional.of(existing);
        }
        return Optional.empty();
    }

    private String key(ConfigCoordinate coordinate) {
        return String.join("::", coordinate.getTenant(), coordinate.getNamespace(), coordinate.getAppId(), coordinate.getKey());
    }
}
