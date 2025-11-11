package io.lighting.config.embedded;

import io.lighting.config.client.transport.ConfigTransport;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.dto.WatchRequest;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.server.notify.NotifyEngine;
import io.lighting.config.server.service.ConfigApplicationService;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * {@link ConfigTransport} implementation backed by in-process services with no gRPC hop.
 */
class EmbeddedConfigTransport implements ConfigTransport {

    private final ConfigApplicationService applicationService;
    private final NotifyEngine notifyEngine;
    private final List<NotifyEngine.Registration> registrations = new CopyOnWriteArrayList<>();

    EmbeddedConfigTransport(ConfigApplicationService applicationService,
                            NotifyEngine notifyEngine) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.notifyEngine = Objects.requireNonNull(notifyEngine, "notifyEngine");
    }

    @Override
    public List<ConfigItem> pull(PullQuery query) {
        return applicationService.list(query);
    }

    @Override
    public WatchHandle watch(WatchRequest request, Consumer<ConfigChange> consumer) {
        NotifyEngine.Subscriber subscriber = new EmbeddedSubscriber(request, consumer);
        NotifyEngine.Registration registration = notifyEngine.register(subscriber);
        registrations.add(registration);
        return () -> {
            registration.close();
            registrations.remove(registration);
        };
    }

    @Override
    public void close() {
        registrations.forEach(NotifyEngine.Registration::close);
        registrations.clear();
    }

    private static final class EmbeddedSubscriber implements NotifyEngine.Subscriber {
        private final WatchRequest request;
        private final Consumer<ConfigChange> consumer;

        private EmbeddedSubscriber(WatchRequest request, Consumer<ConfigChange> consumer) {
            this.request = request;
            this.consumer = consumer;
        }

        @Override
        public boolean matches(io.lighting.config.core.dto.ConfigChangeEvent event) {
            return request.getTenant().equals(event.getCoordinate().getTenant())
                    && request.getNamespace().equals(event.getCoordinate().getNamespace())
                    && request.getAppId().equals(event.getCoordinate().getAppId());
        }

        @Override
        public void onEvent(io.lighting.config.core.dto.ConfigChangeEvent event) {
            consumer.accept(event.getChange());
        }
    }
}
