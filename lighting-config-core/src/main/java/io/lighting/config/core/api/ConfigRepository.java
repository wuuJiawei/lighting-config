package io.lighting.config.core.api;

import io.lighting.config.core.model.ConfigItem;

import java.util.List;
import java.util.Optional;

/**
 * Storage abstraction for authoritative configuration data.
 */
public interface ConfigRepository {

    Optional<ConfigItem> get(String tenant, String namespace, String appId, String key);

    List<ConfigItem> list(String tenant, String namespace, String appId, String prefix);

    void upsert(ConfigItem item, String operator);

    void delete(String tenant, String namespace, String appId, String key, String operator);

    long currentVersion(String tenant, String namespace, String appId, String key);
}
