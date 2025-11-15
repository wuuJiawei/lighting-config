package io.lighting.config.server.repository;

import io.lighting.config.core.model.Revision;

public interface RevisionRepository {

    void save(Revision revision);
}
