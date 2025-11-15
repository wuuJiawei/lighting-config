package io.lighting.config.server.repository;

import io.lighting.config.core.model.Revision;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryRevisionRepository implements RevisionRepository {

    private final List<Revision> revisions = new CopyOnWriteArrayList<>();

    @Override
    public void save(Revision revision) {
        revisions.add(revision);
    }

    public List<Revision> findAll() {
        return List.copyOf(revisions);
    }
}
