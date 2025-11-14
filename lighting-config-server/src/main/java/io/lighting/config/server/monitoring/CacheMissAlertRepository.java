package io.lighting.config.server.monitoring;

import java.util.List;

public interface CacheMissAlertRepository {

    void save(CacheMissAlert alert);

    List<CacheMissAlert> findRecent(String tenant, int limit);
}
