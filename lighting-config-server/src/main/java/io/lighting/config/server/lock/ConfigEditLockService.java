package io.lighting.config.server.lock;

import io.lighting.config.core.api.EventBus;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.util.TimeProvider;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ConfigEditLockService {

    private final ConfigEditLockRepository repository;
    private final EventBus eventBus;
    private final TimeProvider timeProvider;
    private final Duration ttl;
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> expiryTasks = new ConcurrentHashMap<>();

    public ConfigEditLockService(ConfigEditLockRepository repository,
                                 EventBus eventBus,
                                 TimeProvider timeProvider,
                                 Duration ttl) {
        this.repository = repository;
        this.eventBus = eventBus;
        this.timeProvider = timeProvider;
        this.ttl = ttl;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new LockThreadFactory());
    }

    public ConfigEditLock acquire(ConfigCoordinate coordinate, LockOwner owner) {
        Instant now = timeProvider.now();
        handleExpiration(coordinate, now);
        Optional<ConfigEditLock> existing = repository.find(coordinate);
        if (existing.isPresent() && !existing.get().isOwnedBy(owner.getId())) {
            throw new EditLockConflictException(existing.get());
        }
        ConfigEditLock lock = ConfigEditLock.builder()
                .coordinate(coordinate)
                .ownerId(owner.getId())
                .ownerName(owner.getName())
                .expiresAt(now.plus(ttl))
                .updatedAt(now)
                .build();
        repository.upsert(lock);
        scheduleExpiration(lock);
        publish(lock, existing.isPresent() ? LockEventType.REFRESHED : LockEventType.ACQUIRED);
        return lock;
    }

    public boolean release(ConfigCoordinate coordinate, LockOwner owner) {
        Optional<ConfigEditLock> existing = repository.find(coordinate);
        if (existing.isEmpty() || !existing.get().isOwnedBy(owner.getId())) {
            return false;
        }
        boolean removed = repository.deleteIfOwned(coordinate, owner.getId());
        if (removed) {
            cancelExpiration(coordinate);
            publish(existing.get(), LockEventType.RELEASED);
        }
        return removed;
    }

    public Optional<ConfigEditLock> currentLock(ConfigCoordinate coordinate) {
        Instant now = timeProvider.now();
        handleExpiration(coordinate, now);
        Optional<ConfigEditLock> current = repository.find(coordinate);
        current.ifPresent(this::ensureScheduled);
        return current;
    }

    public void ensureEditable(ConfigCoordinate coordinate, LockOwner owner) {
        Optional<ConfigEditLock> lock = currentLock(coordinate);
        if (lock.isPresent() && !lock.get().isOwnedBy(owner.getId())) {
            throw new EditLockConflictException(lock.get());
        }
    }

    private void handleExpiration(ConfigCoordinate coordinate, Instant now) {
        Optional<ConfigEditLock> expired = repository.deleteIfExpired(coordinate, now);
        expired.ifPresent(lock -> {
            cancelExpiration(coordinate);
            publish(lock, LockEventType.EXPIRED);
        });
    }

    private void scheduleExpiration(ConfigEditLock lock) {
        String key = key(lock.getCoordinate());
        cancelExpiration(lock.getCoordinate());
        long delay = Duration.between(timeProvider.now(), lock.getExpiresAt()).toMillis();
        if (delay <= 0) {
            expire(lock);
            return;
        }
        ScheduledFuture<?> future = scheduler.schedule(() -> expire(lock), delay, TimeUnit.MILLISECONDS);
        expiryTasks.put(key, future);
    }

    private void ensureScheduled(ConfigEditLock lock) {
        String key = key(lock.getCoordinate());
        if (!expiryTasks.containsKey(key)) {
            scheduleExpiration(lock);
        }
    }

    private void expire(ConfigEditLock expected) {
        ConfigCoordinate coordinate = expected.getCoordinate();
        Optional<ConfigEditLock> expired = repository.deleteIfExpired(coordinate, timeProvider.now());
        expired.ifPresent(lock -> {
            cancelExpiration(coordinate);
            publish(lock, LockEventType.EXPIRED);
        });
    }

    private void cancelExpiration(ConfigCoordinate coordinate) {
        String key = key(coordinate);
        ScheduledFuture<?> future = expiryTasks.remove(key);
        if (future != null) {
            future.cancel(false);
        }
    }

    private void publish(ConfigEditLock lock, LockEventType type) {
        eventBus.publish(new ConfigEditLockEvent(lock.getCoordinate(), lock, type));
    }

    private String key(ConfigCoordinate coordinate) {
        return String.join("::", coordinate.getTenant(), coordinate.getNamespace(), coordinate.getAppId(), coordinate.getKey());
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    private static class LockThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "config-edit-lock-expiration");
            t.setDaemon(true);
            return t;
        }
    }
}
