package io.lighting.config.embedded;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ContentType;
import io.lighting.config.core.model.ConfigCoordinate;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Lightweight embedded configuration manager that can be hosted inside business apps.
 */
public final class EmbeddedConfigManager implements AutoCloseable {

    private final ConfigRepository repository;
    private final ExecutorService notifier = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "lighting-embedded-notifier");
        t.setDaemon(true);
        return t;
    });
    private final List<Consumer<ConfigChange>> listeners = new CopyOnWriteArrayList<>();

    private EmbeddedConfigManager(ConfigRepository repository) {
        this.repository = repository;
    }

    public static EmbeddedConfigManager create(EmbeddedConfigOptions options) {
        Objects.requireNonNull(options, "options");
        ConfigRepository repository;
        if (options.getStorageType() == EmbeddedConfigOptions.StorageType.FILE) {
            ObjectMapper mapper = new ObjectMapper();
            repository = new FileSystemEmbeddedRepository(options.getStoragePath(), mapper);
        } else {
            repository = new InMemoryEmbeddedRepository();
        }
        return new EmbeddedConfigManager(repository);
    }

    public Optional<ConfigItem> get(String tenant, String namespace, String appId, String key) {
        return repository.get(tenant, namespace, appId, key);
    }

    public List<ConfigItem> list(String tenant, String namespace, String appId, String prefix) {
        return repository.list(tenant, namespace, appId, prefix);
    }

    public ConfigItem upsert(ConfigItem item, String operator) {
        repository.upsert(item, operator);
        ConfigItem persisted = repository.get(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey())
                .orElse(item);
        emitChange(persisted, ChangeType.UPSERT);
        return persisted;
    }

    public void upsert(String tenant, String namespace, String appId, String key, String value, ContentType contentType) {
        ConfigItem item = ConfigItem.builder()
                .tenant(tenant)
                .namespace(namespace)
                .appId(appId)
                .key(key)
                .value(value)
                .contentType(contentType)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        upsert(item, "embedded");
    }

    public void delete(String tenant, String namespace, String appId, String key) {
        repository.delete(tenant, namespace, appId, key, "embedded");
        emitChange(ConfigItem.builder()
                        .tenant(tenant)
                        .namespace(namespace)
                        .appId(appId)
                        .key(key)
                        .contentType(ContentType.TEXT)
                        .value("")
                        .version(repository.currentVersion(tenant, namespace, appId, key))
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build(),
                ChangeType.DELETE);
    }

    public void addListener(Consumer<ConfigChange> listener) {
        listeners.add(listener);
    }

    private void emitChange(ConfigItem item, ChangeType type) {
        ConfigChange change = ConfigChange.builder()
                .coordinate(ConfigCoordinate.of(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey()))
                .version(item.getVersion())
                .type(type)
                .contentType(item.getContentType())
                .value(item.getValue())
                .deleted(type == ChangeType.DELETE)
                .occurredAt(System.currentTimeMillis())
                .build();
        notifier.submit(() -> listeners.forEach(listener -> listener.accept(change)));
    }

    @Override
    public void close() {
        notifier.shutdownNow();
    }
}
