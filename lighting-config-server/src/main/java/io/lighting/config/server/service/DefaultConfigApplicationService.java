package io.lighting.config.server.service;

import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.ConfigChangeEvent;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.util.TimeProvider;
import io.lighting.config.server.notify.ChangeFeed;
import io.lighting.config.server.notify.NotifyEngine;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class DefaultConfigApplicationService implements ConfigApplicationService {

    private final ConfigRepository repository;
    private final NotifyEngine notifyEngine;
    private final ChangeFeed changeFeed;
    private final TimeProvider timeProvider;

    public DefaultConfigApplicationService(ConfigRepository repository,
                                           NotifyEngine notifyEngine,
                                           ChangeFeed changeFeed,
                                           TimeProvider timeProvider) {
        this.repository = repository;
        this.notifyEngine = notifyEngine;
        this.changeFeed = changeFeed;
        this.timeProvider = timeProvider;
    }

    @Override
    public Optional<ConfigItem> get(String tenant, String namespace, String appId, String key) {
        return repository.get(tenant, namespace, appId, key);
    }

    @Override
    public List<ConfigItem> list(PullQuery query) {
        if (query.getSelector().getKey().isPresent()) {
            return repository.get(query.getTenant(), query.getNamespace(), query.getAppId(),
                            query.getSelector().getKey().get())
                    .map(List::of)
                    .orElse(List.of());
        }
        return repository.list(query.getTenant(), query.getNamespace(), query.getAppId(),
                query.getSelector().getPrefix().orElse(null));
    }

    @Override
    public ConfigItem upsert(ConfigItem item, String operator) {
        repository.upsert(item, operator);
        ConfigItem persisted = repository.get(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey())
                .orElse(item);
        publishChange(persisted, ChangeType.UPSERT, operator);
        return persisted;
    }

    @Override
    public void delete(String tenant, String namespace, String appId, String key, String operator) {
        Optional<ConfigItem> existing = repository.get(tenant, namespace, appId, key);
        repository.delete(tenant, namespace, appId, key, operator);
        ConfigCoordinate coordinate = ConfigCoordinate.of(tenant, namespace, appId, key);
        ConfigChange change = ConfigChange.builder()
                .coordinate(coordinate)
                .version(existing.map(ConfigItem::getVersion)
                        .orElse(repository.currentVersion(tenant, namespace, appId, key)))
                .type(ChangeType.DELETE)
                .contentType(existing.map(ConfigItem::getContentType)
                        .orElse(io.lighting.config.core.model.ContentType.TEXT))
                .deleted(true)
                .occurredAt(now())
                .build();
        publish(change, operator);
    }

    private void publishChange(ConfigItem item, ChangeType type, String operator) {
        ConfigChange change = ConfigChange.builder()
                .coordinate(coordinateOf(item))
                .version(item.getVersion())
                .type(type)
                .contentType(item.getContentType())
                .value(item.getValue())
                .occurredAt(now())
                .build();
        publish(change, operator);
    }

    private void publish(ConfigChange change, String operator) {
        changeFeed.append(change);
        notifyEngine.publish(ConfigChangeEvent.builder()
                .change(change)
                .operator(operator)
                .publishedAt(now())
                .build());
    }

    private ConfigCoordinate coordinateOf(ConfigItem item) {
        return ConfigCoordinate.of(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey());
    }

    private Instant now() {
        return timeProvider.now();
    }
}
