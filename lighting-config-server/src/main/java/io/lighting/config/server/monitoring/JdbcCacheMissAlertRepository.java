package io.lighting.config.server.monitoring;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcCacheMissAlertRepository implements CacheMissAlertRepository {

    private static final String INSERT_SQL = "INSERT INTO cache_miss_alert (tenant, namespace, app_id, selector, miss_count, created_at) "
            + "VALUES (:tenant, :namespace, :appId, :selector, :missCount, :createdAt)";
    private static final String SELECT_RECENT_SQL = "SELECT tenant, namespace, app_id, selector, miss_count, created_at "
            + "FROM cache_miss_alert WHERE tenant = :tenant ORDER BY created_at DESC LIMIT :limit";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcCacheMissAlertRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(CacheMissAlert alert) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenant", alert.getTenant())
                .addValue("namespace", alert.getNamespace())
                .addValue("appId", alert.getAppId())
                .addValue("selector", alert.getSelector())
                .addValue("missCount", alert.getMissCount())
                .addValue("createdAt", Timestamp.from(alert.getOccurredAt()));
        jdbcTemplate.update(INSERT_SQL, params);
    }

    @Override
    public List<CacheMissAlert> findRecent(String tenant, int limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("tenant", tenant);
        params.put("limit", limit);
        return jdbcTemplate.query(SELECT_RECENT_SQL, params, (rs, rowNum) -> CacheMissAlert.builder()
                .tenant(rs.getString("tenant"))
                .namespace(rs.getString("namespace"))
                .appId(rs.getString("app_id"))
                .selector(rs.getString("selector"))
                .missCount(rs.getInt("miss_count"))
                .occurredAt(rs.getTimestamp("created_at").toInstant())
                .build());
    }
}
