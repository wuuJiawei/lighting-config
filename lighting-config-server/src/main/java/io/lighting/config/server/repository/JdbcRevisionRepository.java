package io.lighting.config.server.repository;

import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.Revision;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JdbcRevisionRepository implements RevisionRepository {

    private static final String INSERT_SQL = "INSERT INTO revision (tenant, namespace, app_id, key, version, op, operator, diff, created_at) "
            + "VALUES (:tenant, :namespace, :appId, :key, :version, :op, :operator, :diff, :createdAt)";
    private static final String SELECT_BY_COORDINATE = "SELECT * FROM revision WHERE tenant = :tenant AND namespace = :namespace AND app_id = :appId AND key = :key";
    private static final String SELECT_BY_VERSION = SELECT_BY_COORDINATE + " AND version = :version";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Revision> rowMapper = (rs, rowNum) -> Revision.builder()
            .coordinate(ConfigCoordinate.of(
                    rs.getString("tenant"),
                    rs.getString("namespace"),
                    rs.getString("app_id"),
                    rs.getString("key")))
            .version(rs.getLong("version"))
            .operation(io.lighting.config.core.model.RevisionOperation.valueOf(rs.getString("op")))
            .operator(rs.getString("operator"))
            .diff(rs.getString("diff"))
            .createdAt(rs.getTimestamp("created_at").toInstant())
            .build();

    public JdbcRevisionRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Revision revision) {
        ConfigCoordinate coordinate = revision.getCoordinate();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenant", coordinate.getTenant())
                .addValue("namespace", coordinate.getNamespace())
                .addValue("appId", coordinate.getAppId())
                .addValue("key", coordinate.getKey())
                .addValue("version", revision.getVersion())
                .addValue("op", revision.getOperation().name())
                .addValue("operator", revision.getOperator())
                .addValue("diff", revision.getDiff(), Types.OTHER)
                .addValue("createdAt", Timestamp.from(revision.getCreatedAt()));
        jdbcTemplate.update(INSERT_SQL, params);
    }

    @Override
    public List<Revision> listByCoordinate(String tenant, String namespace, String appId, String key) {
        MapSqlParameterSource params = baseParams(tenant, namespace, appId).addValue("key", key);
        return jdbcTemplate.query(SELECT_BY_COORDINATE, params, rowMapper).stream()
                .sorted(Comparator.comparingLong(Revision::getVersion).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Revision> findByCoordinateAndVersion(String tenant, String namespace, String appId, String key, long version) {
        MapSqlParameterSource params = baseParams(tenant, namespace, appId).addValue("key", key).addValue("version", version);
        List<Revision> results = jdbcTemplate.query(SELECT_BY_VERSION, params, rowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    private MapSqlParameterSource baseParams(String tenant, String namespace, String appId) {
        return new MapSqlParameterSource()
                .addValue("tenant", tenant)
                .addValue("namespace", namespace)
                .addValue("appId", appId);
    }
}
