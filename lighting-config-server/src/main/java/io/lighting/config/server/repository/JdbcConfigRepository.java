package io.lighting.config.server.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ContentType;
import io.lighting.config.core.util.TimeProvider;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcConfigRepository implements ConfigRepository {

    private static final String SELECT_ONE = "SELECT * FROM config_item WHERE tenant = :tenant AND namespace = :namespace AND app_id = :appId AND key = :key";
    private static final String SELECT_PREFIX = "SELECT * FROM config_item WHERE tenant = :tenant AND namespace = :namespace AND app_id = :appId AND (:prefix IS NULL OR key LIKE :prefix) ORDER BY key";
    private static final String INSERT_SQL = "INSERT INTO config_item (tenant, namespace, app_id, key, content_type, value, version, tags, enabled, created_at, updated_at) " +
            "VALUES (:tenant, :namespace, :appId, :key, :contentType, :value, :version, :tags, :enabled, :createdAt, :updatedAt)";
    private static final String UPDATE_SQL = "UPDATE config_item SET value = :value, content_type = :contentType, tags = :tags, enabled = :enabled, version = :version, updated_at = :updatedAt " +
            "WHERE tenant = :tenant AND namespace = :namespace AND app_id = :appId AND key = :key";
    private static final String DELETE_SQL = "DELETE FROM config_item WHERE tenant = :tenant AND namespace = :namespace AND app_id = :appId AND key = :key";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final TimeProvider timeProvider;
    private final RowMapper<ConfigItem> rowMapper = new ConfigItemRowMapper();

    public JdbcConfigRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                ObjectMapper objectMapper,
                                TimeProvider timeProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.timeProvider = timeProvider;
    }

    @Override
    public Optional<ConfigItem> get(String tenant, String namespace, String appId, String key) {
        MapSqlParameterSource params = baseParams(tenant, namespace, appId).addValue("key", key);
        List<ConfigItem> results = jdbcTemplate.query(SELECT_ONE, params, rowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<ConfigItem> list(String tenant, String namespace, String appId, String prefix) {
        MapSqlParameterSource params = baseParams(tenant, namespace, appId);
        params.addValue("prefix", prefix == null ? null : prefix + "%");
        return jdbcTemplate.query(SELECT_PREFIX, params, rowMapper);
    }

    @Override
    public void upsert(ConfigItem item, String operator) {
        Optional<ConfigItem> existing = get(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey());
        long nextVersion = existing.map(ConfigItem::getVersion).map(v -> v + 1).orElse(1L);
        Instant now = timeProvider.now();
        MapSqlParameterSource params = toParamSource(item, nextVersion, now, existing.map(ConfigItem::getCreatedAt).orElse(now));
        if (existing.isPresent()) {
            jdbcTemplate.update(UPDATE_SQL, params);
        } else {
            jdbcTemplate.update(INSERT_SQL, params);
        }
    }

    @Override
    public void delete(String tenant, String namespace, String appId, String key, String operator) {
        MapSqlParameterSource params = baseParams(tenant, namespace, appId).addValue("key", key);
        jdbcTemplate.update(DELETE_SQL, params);
    }

    @Override
    public long currentVersion(String tenant, String namespace, String appId, String key) {
        return get(tenant, namespace, appId, key)
                .map(ConfigItem::getVersion)
                .orElse(0L);
    }

    private MapSqlParameterSource baseParams(String tenant, String namespace, String appId) {
        return new MapSqlParameterSource()
                .addValue("tenant", tenant)
                .addValue("namespace", namespace)
                .addValue("appId", appId);
    }

    private MapSqlParameterSource toParamSource(ConfigItem item, long version, Instant updatedAt, Instant createdAt) {
        return baseParams(item.getTenant(), item.getNamespace(), item.getAppId())
                .addValue("key", item.getKey())
                .addValue("contentType", item.getContentType().name())
                .addValue("value", item.getValue())
                .addValue("version", version)
                .addValue("tags", serializeTags(item.getLabels()))
                .addValue("enabled", item.isEnabled())
                .addValue("createdAt", createdAt)
                .addValue("updatedAt", updatedAt);
    }

    private String serializeTags(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(labels);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize labels", e);
        }
    }

    private Map<String, String> parseTags(String payload) {
        if (payload == null || payload.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(payload, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    private class ConfigItemRowMapper implements RowMapper<ConfigItem> {
        @Override
        public ConfigItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ConfigItem.builder()
                    .tenant(rs.getString("tenant"))
                    .namespace(rs.getString("namespace"))
                    .appId(rs.getString("app_id"))
                    .key(rs.getString("key"))
                    .contentType(ContentType.fromAlias(rs.getString("content_type")))
                    .value(rs.getString("value"))
                    .version(rs.getLong("version"))
                    .labels(parseTags(rs.getString("tags")))
                    .enabled(rs.getBoolean("enabled"))
                    .createdAt(rs.getTimestamp("created_at").toInstant())
                    .updatedAt(rs.getTimestamp("updated_at").toInstant())
                    .build();
        }
    }
}
