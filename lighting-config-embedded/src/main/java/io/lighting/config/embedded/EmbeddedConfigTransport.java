package io.lighting.config.embedded;

import io.lighting.config.client.transport.PollingTransport;
import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PollAdvice;
import io.lighting.config.core.dto.PollRequest;
import io.lighting.config.core.dto.PollResponse;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ContentType;
import io.lighting.config.server.notify.NotifyEngine;
import io.lighting.config.server.service.ConfigApplicationService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link PollingTransport} implementation that works entirely in-process for embedded mode.
 */
class EmbeddedConfigTransport implements PollingTransport {

    private final ConfigApplicationService applicationService;
    private final List<ConfigChange> pendingEvents = new CopyOnWriteArrayList<>();
    private final NotifyEngine.Registration registration;
    private final Duration defaultInterval = Duration.ofSeconds(5);

    EmbeddedConfigTransport(ConfigApplicationService applicationService,
                            NotifyEngine notifyEngine) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.registration = notifyEngine.register(new InMemorySubscriber());
    }

    @Override
    public PollResponse poll(PollRequest request) {
        List<ConfigChange> changes;
        long version = request.getLastVersion();
        if (request.getLastVersion() == 0) {
            changes = snapshot(request);
            version = latestVersion(changes, version);
        } else {
            changes = drainPending(request.getLastVersion());
            version = latestVersion(changes, version);
        }
        return PollResponse.builder()
                .version(version)
                .items(changes)
                .advice(PollAdvice.builder().nextInterval(defaultInterval).build())
                .build();
    }

    private List<ConfigChange> snapshot(PollRequest request) {
        PullQuery query = PullQuery.builder()
                .tenant(request.getTenant())
                .namespace(request.getNamespace())
                .appId(request.getAppId())
                .build();
        List<ConfigChange> changes = new ArrayList<>();
        for (ConfigItem item : applicationService.list(query)) {
            changes.add(toChange(item));
        }
        pendingEvents.clear();
        return changes;
    }

    private List<ConfigChange> drainPending(long lastVersion) {
        List<ConfigChange> result = new ArrayList<>();
        pendingEvents.removeIf(change -> {
            if (change.getVersion() > lastVersion) {
                result.add(change);
                return true;
            }
            return false;
        });
        return result;
    }

    private long latestVersion(List<ConfigChange> changes, long current) {
        long version = current;
        for (ConfigChange change : changes) {
            version = Math.max(version, change.getVersion());
        }
        return version;
    }

    private ConfigChange toChange(ConfigItem item) {
        return ConfigChange.builder()
                .coordinate(ConfigCoordinate.of(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey()))
                .version(item.getVersion())
                .type(item.isEnabled() ? ChangeType.UPSERT : ChangeType.DELETE)
                .contentType(item.getContentType())
                .value(item.getValue())
                .deleted(!item.isEnabled())
                .occurredAt(item.getUpdatedAt())
                .build();
    }

    @Override
    public void close() {
        registration.close();
    }

    private final class InMemorySubscriber implements NotifyEngine.Subscriber {
        @Override
        public boolean matches(io.lighting.config.core.dto.ConfigChangeEvent event) {
            return true;
        }

        @Override
        public void onEvent(io.lighting.config.core.dto.ConfigChangeEvent event) {
            pendingEvents.add(event.getChange());
        }
    }
}
