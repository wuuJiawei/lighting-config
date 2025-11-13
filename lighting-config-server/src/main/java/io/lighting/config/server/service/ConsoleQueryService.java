package io.lighting.config.server.service;

import io.lighting.config.server.rest.dto.AuditRecordResponse;
import io.lighting.config.server.rest.dto.DashboardStatView;
import io.lighting.config.server.rest.dto.NamespaceSummaryResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@ConditionalOnBean(DataSource.class)
public class ConsoleQueryService {

    private static final String DASHBOARD_SQL = "SELECT COUNT(*) AS total, "
            + "SUM(CASE WHEN enabled THEN 1 ELSE 0 END) AS active, "
            + "SUM(CASE WHEN NOT enabled THEN 1 ELSE 0 END) AS inactive, "
            + "MAX(updated_at) AS latest "
            + "FROM config_item WHERE tenant = :tenant";

    private static final String NAMESPACE_SQL = "SELECT namespace, app_id, COUNT(*) AS config_count, "
            + "SUM(CASE WHEN enabled THEN 1 ELSE 0 END) AS active_count, MAX(updated_at) AS updated_at "
            + "FROM config_item WHERE tenant = :tenant GROUP BY namespace, app_id ORDER BY namespace, app_id";

    private static final String AUDIT_SQL = "SELECT namespace, app_id, key, enabled, version, updated_at "
            + "FROM config_item WHERE tenant = :tenant ORDER BY updated_at DESC LIMIT :limit";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ConsoleQueryService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DashboardStatView> fetchDashboardStats(String tenant) {
        Map<String, Object> params = Map.of("tenant", tenant);
        return jdbcTemplate.query(DASHBOARD_SQL, params, rs -> {
            List<DashboardStatView> stats = new ArrayList<>();
            if (rs.next()) {
                long total = rs.getLong("total");
                long active = rs.getLong("active");
                long inactive = rs.getLong("inactive");
                Timestamp latest = rs.getTimestamp("latest");
                stats.add(new DashboardStatView("配置总数", Long.toString(total), 0, "up", "含所有命名空间"));
                stats.add(new DashboardStatView("已启用", Long.toString(active), 0, "up", "当前可投递"));
                stats.add(new DashboardStatView("停用配置", Long.toString(inactive), 0, inactive > 0 ? "down" : "up", "含灰度/清理中"));
                stats.add(new DashboardStatView("最近发布", latest == null ? "--" : latest.toInstant().toString(), 0, "up", "最后一次更新时间"));
            }
            if (stats.isEmpty()) {
                stats.add(new DashboardStatView("配置总数", "0", 0, "up", "含所有命名空间"));
                stats.add(new DashboardStatView("已启用", "0", 0, "up", "当前可投递"));
                stats.add(new DashboardStatView("停用配置", "0", 0, "up", "含灰度/清理中"));
                stats.add(new DashboardStatView("最近发布", "--", 0, "up", "最后一次更新时间"));
            }
            return stats;
        });
    }

    public List<NamespaceSummaryResponse> fetchNamespaces(String tenant) {
        Map<String, Object> params = Map.of("tenant", tenant);
        return jdbcTemplate.query(NAMESPACE_SQL, params, rs -> {
            Map<String, NamespaceAccumulator> grouped = new LinkedHashMap<>();
            while (rs.next()) {
                String namespace = rs.getString("namespace");
                String appId = rs.getString("app_id");
                long count = rs.getLong("config_count");
                long active = rs.getLong("active_count");
                Timestamp updated = rs.getTimestamp("updated_at");
                NamespaceAccumulator acc = grouped.computeIfAbsent(namespace, key -> new NamespaceAccumulator());
                acc.configCount += count;
                acc.watchers += active;
                acc.appIds.add(appId);
                acc.updatedAt = latest(acc.updatedAt, updated == null ? null : updated.toInstant());
            }
            List<NamespaceSummaryResponse> responses = new ArrayList<>();
            grouped.forEach((name, acc) -> responses.add(new NamespaceSummaryResponse(
                    name,
                    "-",
                    acc.configCount,
                    acc.watchers,
                    new ArrayList<>(acc.appIds),
                    acc.updatedAt == null ? Instant.EPOCH : acc.updatedAt
            )));
            return responses;
        });
    }

    public List<AuditRecordResponse> fetchAuditTrail(String tenant, int limit) {
        Map<String, Object> params = Map.of("tenant", tenant, "limit", limit);
        return jdbcTemplate.query(AUDIT_SQL, params, (rs, rowNum) -> {
            String namespace = rs.getString("namespace");
            String appId = rs.getString("app_id");
            String key = rs.getString("key");
            boolean enabled = rs.getBoolean("enabled");
            long version = rs.getLong("version");
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            String action = enabled ? "PUBLISH" : "DELETE";
            String message = enabled ? String.format(Locale.ROOT, "发布版本 v%d", version)
                    : "删除配置";
            String id = namespace + ":" + appId + ":" + key + ":" + version;
            return new AuditRecordResponse(id, namespace, appId, key, "system", action, message,
                    updatedAt == null ? Instant.now() : updatedAt.toInstant());
        });
    }

    private Instant latest(Instant current, Instant next) {
        if (current == null) {
            return next;
        }
        if (next == null) {
            return current;
        }
        return current.isAfter(next) ? current : next;
    }

    private static class NamespaceAccumulator {
        private long configCount = 0L;
        private long watchers = 0L;
        private final Set<String> appIds = new LinkedHashSet<>();
        private Instant updatedAt;
    }
}
