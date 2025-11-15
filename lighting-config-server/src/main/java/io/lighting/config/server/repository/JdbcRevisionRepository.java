package io.lighting.config.server.repository;

import io.lighting.config.core.model.Revision;
import io.lighting.config.core.model.ConfigCoordinate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.sql.Types;

public class JdbcRevisionRepository implements RevisionRepository {

    private static final String INSERT_SQL = "INSERT INTO revision (tenant, namespace, app_id, key, version, op, operator, diff, created_at) "
            + "VALUES (:tenant, :namespace, :appId, :key, :version, :op, :operator, :diff, :createdAt)";

    private final NamedParameterJdbcTemplate jdbcTemplate;

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
}
