package io.lighting.config.embedded;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ContentType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

final class FileSystemEmbeddedRepository implements ConfigRepository {

    private final Path path;
    private final ObjectMapper objectMapper;
    private final Map<String, ConfigItem> store = new ConcurrentHashMap<>();
    private final AtomicLong versionGenerator = new AtomicLong(0);

    FileSystemEmbeddedRepository(Path path, ObjectMapper objectMapper) {
        this.path = path;
        this.objectMapper = objectMapper;
        load();
    }

    private void load() {
        if (!Files.exists(path)) {
            return;
        }
        try {
            List<ConfigItem> items = objectMapper.readValue(Files.readAllBytes(path), new TypeReference<List<ConfigItem>>() {
            });
            for (ConfigItem item : items) {
                store.put(k(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey()), item);
                versionGenerator.updateAndGet(v -> Math.max(v, item.getVersion()));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config file: " + path, e);
        }
    }

    private void persist() {
        try {
            List<ConfigItem> snapshot = new ArrayList<>(store.values());
            Files.createDirectories(path.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), snapshot);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to persist config file: " + path, e);
        }
    }

    @Override
    public Optional<ConfigItem> get(String tenant, String namespace, String appId, String key) {
        return Optional.ofNullable(store.get(k(tenant, namespace, appId, key)));
    }

    @Override
    public List<ConfigItem> list(String tenant, String namespace, String appId, String prefix) {
        return store.values().stream()
                .filter(item -> item.getTenant().equals(tenant)
                        && item.getNamespace().equals(namespace)
                        && item.getAppId().equals(appId)
                        && (prefix == null || item.getKey().startsWith(prefix)))
                .sorted(Comparator.comparing(ConfigItem::getKey))
                .collect(Collectors.toList());
    }

    @Override
    public void upsert(ConfigItem item, String operator) {
        Instant now = Instant.now();
        long version = versionGenerator.incrementAndGet();
        ConfigItem persisted = item.toBuilder()
                .version(version)
                .createdAt(item.getCreatedAt() == null ? now : item.getCreatedAt())
                .updatedAt(now)
                .build();
        store.put(k(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey()), persisted);
        persist();
    }

    @Override
    public void delete(String tenant, String namespace, String appId, String key, String operator) {
        store.remove(k(tenant, namespace, appId, key));
        persist();
    }

    @Override
    public long currentVersion(String tenant, String namespace, String appId, String key) {
        return get(tenant, namespace, appId, key).map(ConfigItem::getVersion).orElse(0L);
    }

    private String k(String tenant, String namespace, String appId, String key) {
        return tenant + "|" + namespace + "|" + appId + "|" + key;
    }
}
