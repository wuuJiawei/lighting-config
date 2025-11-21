package io.lighting.config.server.repository;

import io.lighting.config.core.model.Revision;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class InMemoryRevisionRepository implements RevisionRepository {

    private final List<Revision> revisions = new CopyOnWriteArrayList<>();

    @Override
    public void save(Revision revision) {
        revisions.add(revision);
    }

    @Override
    public List<Revision> listByCoordinate(String tenant, String namespace, String appId, String key) {
        return revisions.stream()
                .filter(rev -> rev.getCoordinate().getTenant().equals(tenant))
                .filter(rev -> rev.getCoordinate().getNamespace().equals(namespace))
                .filter(rev -> rev.getCoordinate().getAppId().equals(appId))
                .filter(rev -> rev.getCoordinate().getKey().equals(key))
                .sorted(Comparator.comparingLong(Revision::getVersion).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Revision> findByCoordinateAndVersion(String tenant, String namespace, String appId, String key, long version) {
        return listByCoordinate(tenant, namespace, appId, key).stream()
                .filter(rev -> rev.getVersion() == version)
                .findFirst();
    }
}
