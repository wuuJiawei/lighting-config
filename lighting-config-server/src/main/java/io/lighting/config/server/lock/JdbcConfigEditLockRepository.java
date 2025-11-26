package io.lighting.config.server.lock;

import io.lighting.config.core.model.ConfigCoordinate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class JdbcConfigEditLockRepository implements ConfigEditLockRepository {

    private static final String SELECT_SQL = "SELECT * FROM config_edit_lock WHERE tenant = :tenant AND namespace = :namespace AND app_id = :appId AND key = :key";
    private static final String UPDATE_SQL = "UPDATE config_edit_lock SET owner_id = :ownerId, owner_name = :ownerName, expires_at = :expiresAt, updated_at = :updatedAt "
            + "WHERE tenant = :tenant AND namespace = :namespace AND app_id = :appId AND key = :key";
    private static final String INSERT_SQL = "INSERT INTO config_edit_lock (tenant, namespace, app_id, key, owner_id, owner_name, expires_at, updated_at) "
            + "VALUES (:tenant, :namespace, :appId, :key, :ownerId, :ownerName, :expiresAt, :updatedAt)";
    private static final String DELETE_OWNED_SQL = "DELETE FROM config_edit_lock WHERE tenant = :tenant AND namespace = :namespace AND app_id = :appId AND key = :key AND owner_id = :ownerId";
    private static final String DELETE_EXPIRED_SQL = "DELETE FROM config_edit_lock WHERE tenant = :tenant AND namespace = :namespace AND app_id = :appId AND key = :key AND expires_at <= :now";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<ConfigEditLock> rowMapper = (rs, rowNum) -> ConfigEditLock.builder()
            .coordinate(ConfigCoordinate.of(
                    rs.getString("tenant"),
                    rs.getString("namespace"),
                    rs.getString("app_id"),
                    rs.getString("key")))
            .ownerId(rs.getString("owner_id"))
            .ownerName(rs.getString("owner_name"))
            .expiresAt(rs.getTimestamp("expires_at").toInstant())
            .updatedAt(rs.getTimestamp("updated_at").toInstant())
            .build();

    public JdbcConfigEditLockRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ConfigEditLock> find(ConfigCoordinate coordinate) {
        List<ConfigEditLock> results = jdbcTemplate.query(SELECT_SQL, baseParams(coordinate), rowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void upsert(ConfigEditLock lock) {
        MapSqlParameterSource params = toParamSource(lock);
        int updated = jdbcTemplate.update(UPDATE_SQL, params);
        if (updated > 0) {
            return;
        }
        try {
            jdbcTemplate.update(INSERT_SQL, params);
        } catch (DuplicateKeyException duplicate) {
            jdbcTemplate.update(UPDATE_SQL, params);
        }
    }

    @Override
    public boolean deleteIfOwned(ConfigCoordinate coordinate, String ownerId) {
        MapSqlParameterSource params = baseParams(coordinate).addValue("ownerId", ownerId);
        int affected = jdbcTemplate.update(DELETE_OWNED_SQL, params);
        return affected > 0;
    }

    @Override
    public Optional<ConfigEditLock> deleteIfExpired(ConfigCoordinate coordinate, Instant now) {
        Optional<ConfigEditLock> existing = find(coordinate);
        if (existing.isEmpty() || !existing.get().isExpired(now)) {
            return Optional.empty();
        }
        MapSqlParameterSource params = baseParams(coordinate).addValue("now", Timestamp.from(now));
        int affected = jdbcTemplate.update(DELETE_EXPIRED_SQL, params);
        if (affected > 0) {
            return existing;
        }
        return Optional.empty();
    }

    private MapSqlParameterSource toParamSource(ConfigEditLock lock) {
        return baseParams(lock.getCoordinate())
                .addValue("ownerId", lock.getOwnerId())
                .addValue("ownerName", lock.getOwnerName())
                .addValue("expiresAt", Timestamp.from(lock.getExpiresAt()))
                .addValue("updatedAt", Timestamp.from(lock.getUpdatedAt()));
    }

    private MapSqlParameterSource baseParams(ConfigCoordinate coordinate) {
        return new MapSqlParameterSource()
                .addValue("tenant", coordinate.getTenant())
                .addValue("namespace", coordinate.getNamespace())
                .addValue("appId", coordinate.getAppId())
                .addValue("key", coordinate.getKey());
    }
}
