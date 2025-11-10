package io.lighting.config.server.service;

import io.lighting.config.core.dto.ConfigSelector;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.model.ConfigItem;

import java.util.List;
import java.util.Optional;

public interface ConfigApplicationService {

    Optional<ConfigItem> get(String tenant, String namespace, String appId, String key);

    List<ConfigItem> list(PullQuery query);

    ConfigItem upsert(ConfigItem item, String operator);

    void delete(String tenant, String namespace, String appId, String key, String operator);

    default List<ConfigItem> listByPrefix(String tenant, String namespace, String appId, String prefix) {
        PullQuery query = PullQuery.builder()
                .tenant(tenant)
                .namespace(namespace)
                .appId(appId)
                .selector(ConfigSelector.byPrefix(prefix))
                .build();
        return list(query);
    }
}
