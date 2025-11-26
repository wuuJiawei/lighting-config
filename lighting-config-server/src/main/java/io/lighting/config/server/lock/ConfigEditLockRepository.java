package io.lighting.config.server.lock;

import io.lighting.config.core.model.ConfigCoordinate;

import java.time.Instant;
import java.util.Optional;

public interface ConfigEditLockRepository {

    Optional<ConfigEditLock> find(ConfigCoordinate coordinate);

    void upsert(ConfigEditLock lock);

    boolean deleteIfOwned(ConfigCoordinate coordinate, String ownerId);

    Optional<ConfigEditLock> deleteIfExpired(ConfigCoordinate coordinate, Instant now);
}
