package io.lighting.config.server.lock;

import io.lighting.config.core.api.EventBus;
import io.lighting.config.core.api.EventBus.Subscription;
import io.lighting.config.core.model.ConfigCoordinate;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class ConfigEditLockNotifier {

    private static final Logger log = LoggerFactory.getLogger(ConfigEditLockNotifier.class);
    private static final long SSE_TIMEOUT = TimeUnit.MINUTES.toMillis(15);

    private final ConfigEditLockService lockService;
    private final EventBus eventBus;
    private final Map<String, List<Watcher>> watchers = new ConcurrentHashMap<>();
    private final Subscription subscription;

    public ConfigEditLockNotifier(ConfigEditLockService lockService, EventBus eventBus) {
        this.lockService = lockService;
        this.eventBus = eventBus;
        this.subscription = this.eventBus.subscribe(ConfigEditLockEvent.class, this::broadcast);
    }

    public SseEmitter watch(ConfigCoordinate coordinate, LockOwner requester) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        Watcher watcher = new Watcher(requester, emitter);
        String mapKey = key(coordinate);
        watchers.computeIfAbsent(mapKey, k -> new CopyOnWriteArrayList<>()).add(watcher);
        emitter.onCompletion(() -> removeWatcher(mapKey, watcher));
        emitter.onTimeout(() -> removeWatcher(mapKey, watcher));
        emitter.onError((error) -> removeWatcher(mapKey, watcher));
        sendSnapshot(coordinate, watcher);
        return emitter;
    }

    private void broadcast(ConfigEditLockEvent event) {
        String mapKey = key(event.getCoordinate());
        List<Watcher> targets = watchers.get(mapKey);
        if (targets == null || targets.isEmpty()) {
            return;
        }
        for (Watcher watcher : targets) {
            emit(buildView(event, watcher.owner), watcher, mapKey);
        }
    }

    private void sendSnapshot(ConfigCoordinate coordinate, Watcher watcher) {
        Optional<ConfigEditLock> lock = lockService.currentLock(coordinate);
        ConfigEditLockView view = lock
                .map(value -> ConfigEditLockView.from(value, watcher.owner, LockEventType.SNAPSHOT))
                .orElse(ConfigEditLockView.unlocked(coordinate, watcher.owner, LockEventType.SNAPSHOT));
        emit(view, watcher, key(coordinate));
    }

    private ConfigEditLockView buildView(ConfigEditLockEvent event, LockOwner requester) {
        if (event.getLock() == null) {
            return ConfigEditLockView.unlocked(event.getCoordinate(), requester, event.getType());
        }
        return ConfigEditLockView.from(event.getLock(), requester, event.getType());
    }

    private void emit(ConfigEditLockView view, Watcher watcher, String mapKey) {
        try {
            watcher.emitter.send(SseEmitter.event()
                    .name("lock")
                    .data(view)
                    .id(view.getEvent())
                    .reconnectTime(1500));
        } catch (IOException e) {
            log.debug("SSE send failed, removing watcher {} for {}", watcher.owner.getId(), mapKey, e);
            watcher.emitter.completeWithError(e);
            removeWatcher(mapKey, watcher);
        }
    }

    private void removeWatcher(String mapKey, Watcher watcher) {
        List<Watcher> list = watchers.get(mapKey);
        if (list != null) {
            list.remove(watcher);
            if (list.isEmpty()) {
                watchers.remove(mapKey);
            }
        }
    }

    private String key(ConfigCoordinate coordinate) {
        return String.join("::", coordinate.getTenant(), coordinate.getNamespace(), coordinate.getAppId(), coordinate.getKey());
    }

    @PreDestroy
    public void shutdown() {
        subscription.close();
        watchers.values().forEach(list -> list.forEach(w -> w.emitter.complete()));
        watchers.clear();
    }

    private static class Watcher {
        private final LockOwner owner;
        private final SseEmitter emitter;

        Watcher(LockOwner owner, SseEmitter emitter) {
            this.owner = owner;
            this.emitter = emitter;
        }
    }
}
