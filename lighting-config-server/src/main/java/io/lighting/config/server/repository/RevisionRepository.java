package io.lighting.config.server.repository;

import io.lighting.config.core.model.Revision;
import java.util.List;
import java.util.Optional;

public interface RevisionRepository {

    void save(Revision revision);

    List<Revision> listByCoordinate(String tenant, String namespace, String appId, String key);

    Optional<Revision> findByCoordinateAndVersion(String tenant, String namespace, String appId, String key, long version);
}
