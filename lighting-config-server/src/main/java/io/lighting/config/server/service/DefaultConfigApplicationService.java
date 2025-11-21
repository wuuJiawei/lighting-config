package io.lighting.config.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.ConfigChangeEvent;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ContentType;
import io.lighting.config.core.model.Revision;
import io.lighting.config.core.model.RevisionOperation;
import io.lighting.config.core.util.TimeProvider;
import io.lighting.config.server.cache.ClientSnapshotService;
import io.lighting.config.server.notify.ChangeFeed;
import io.lighting.config.server.notify.NotifyEngine;
import io.lighting.config.server.repository.RevisionRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Map;

public class DefaultConfigApplicationService implements ConfigApplicationService {

    private final ConfigRepository repository;
    private final NotifyEngine notifyEngine;
    private final ChangeFeed changeFeed;
    private final TimeProvider timeProvider;
    private final ClientSnapshotService clientSnapshotService;
    private final RevisionRepository revisionRepository;
    private final ObjectMapper objectMapper;

    public DefaultConfigApplicationService(ConfigRepository repository,
                                           NotifyEngine notifyEngine,
                                           ChangeFeed changeFeed,
                                           TimeProvider timeProvider,
                                           ClientSnapshotService clientSnapshotService,
                                           RevisionRepository revisionRepository,
                                           ObjectMapper objectMapper) {
        this.repository = repository;
        this.notifyEngine = notifyEngine;
        this.changeFeed = changeFeed;
        this.timeProvider = timeProvider;
        this.clientSnapshotService = clientSnapshotService;
        this.revisionRepository = revisionRepository;
        this.objectMapper = objectMapper;
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
        Optional<ConfigItem> existing = repository.get(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey());
        repository.upsert(item, operator);
        ConfigItem persisted = repository.get(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey())
                .orElse(item);
        publishChange(persisted, ChangeType.UPSERT, operator);
        recordRevision(existing.orElse(null), persisted, RevisionOperation.UPSERT, operator);
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
                        .orElse(io.lighting.config.core.model.ContentType.STRING))
                .deleted(true)
                .occurredAt(nowMillis())
                .build();
        publish(change, operator);
        clientSnapshotService.invalidate(coordinate);
        recordRevision(existing.orElse(null), null, RevisionOperation.DELETE, operator);
    }

    private void publishChange(ConfigItem item, ChangeType type, String operator) {
        ConfigChange change = ConfigChange.builder()
                .coordinate(coordinateOf(item))
                .version(item.getVersion())
                .type(type)
                .contentType(item.getContentType())
                .value(item.getValue())
                .occurredAt(nowMillis())
                .build();
        publish(change, operator);
        clientSnapshotService.invalidate(coordinateOf(item));
    }

    private void publish(ConfigChange change, String operator) {
        changeFeed.append(change);
        notifyEngine.publish(ConfigChangeEvent.builder()
                .change(change)
                .operator(operator)
                .publishedAt(nowMillis())
                .build());
    }

    private ConfigCoordinate coordinateOf(ConfigItem item) {
        return ConfigCoordinate.of(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey());
    }

    private long nowMillis() {
        return timeProvider.now().toEpochMilli();
    }

    private Instant nowInstant() {
        return timeProvider.now();
    }

    private void recordRevision(ConfigItem before, ConfigItem after, RevisionOperation operation, String operator) {
        ConfigItem reference = after != null ? after : before;
        if (reference == null) {
            return;
        }
        Revision.Builder builder = Revision.builder()
                .coordinate(coordinateOf(reference))
                .operation(operation)
                .operator(operator)
                .diff(buildDiff(before, after))
                .createdAt(nowInstant());
        if (operation == RevisionOperation.DELETE && before != null) {
            builder.version(before.getVersion());
        } else {
            builder.version(reference.getVersion());
        }
        revisionRepository.save(builder.build());
    }

    private String buildDiff(ConfigItem before, ConfigItem after) {
        ObjectNode root = objectMapper.createObjectNode();
        if (before != null) {
            root.set("before", snapshot(before));
        }
        if (after != null) {
            root.set("after", snapshot(after));
        }
        return root.size() == 0 ? "" : root.toString();
    }

    private ObjectNode snapshot(ConfigItem item) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("value", item.getValue());
        node.put("contentType", item.getContentType().name());
        node.put("enabled", item.isEnabled());
        node.put("version", item.getVersion());
        node.set("labels", objectMapper.valueToTree(item.getLabels()));
        return node;
    }

    @Override
    public List<Revision> listRevisions(String tenant, String namespace, String appId, String key) {
        return revisionRepository.listByCoordinate(tenant, namespace, appId, key);
    }

    @Override
    public ConfigItem rollback(String tenant, String namespace, String appId, String key, long targetVersion, String operator) {
        Revision revision = revisionRepository.findByCoordinateAndVersion(tenant, namespace, appId, key, targetVersion)
                .orElseThrow(() -> new IllegalArgumentException("Revision not found for target version " + targetVersion));
        ConfigItem targetSnapshot = toConfigFromRevision(revision);
        if (targetSnapshot == null) {
            throw new IllegalStateException("Revision diff missing snapshot for rollback");
        }
        ConfigItem rollbackSource = targetSnapshot.toBuilder()
                .tenant(tenant)
                .namespace(namespace)
                .appId(appId)
                .key(key)
                .updatedAt(nowInstant())
                .build();
        ConfigItem persisted = upsert(rollbackSource, operator);
        return persisted;
    }

    private ConfigItem toConfigFromRevision(Revision revision) {
        String diff = revision.getDiff();
        if (diff == null || diff.isEmpty()) {
            return null;
        }
        try {
            ObjectNode root = objectMapper.readValue(diff, ObjectNode.class);
            ObjectNode snapshot = chooseSnapshot(revision.getOperation(), root);
            if (snapshot == null) {
                return null;
            }
            return ConfigItem.builder()
                    .tenant(revision.getCoordinate().getTenant())
                    .namespace(revision.getCoordinate().getNamespace())
                    .appId(revision.getCoordinate().getAppId())
                    .key(revision.getCoordinate().getKey())
                    .contentType(ContentType.fromAlias(snapshot.path("contentType").asText(ContentType.STRING.name())))
                    .value(snapshot.path("value").asText(""))
                    .labels(objectMapper.convertValue(snapshot.path("labels"), objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class)))
                    .enabled(snapshot.path("enabled").asBoolean(true))
                    .version(snapshot.path("version").asLong(revision.getVersion()))
                    .createdAt(nowInstant())
                    .updatedAt(nowInstant())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse revision diff", e);
        }
    }

    private ObjectNode chooseSnapshot(RevisionOperation operation, ObjectNode root) {
        if (operation == RevisionOperation.DELETE && root.has("before")) {
            return (ObjectNode) root.get("before");
        }
        if (root.has("after")) {
            return (ObjectNode) root.get("after");
        }
        if (root.has("before")) {
            return (ObjectNode) root.get("before");
        }
        return null;
    }
}
