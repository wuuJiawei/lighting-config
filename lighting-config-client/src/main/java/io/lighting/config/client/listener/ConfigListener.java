package io.lighting.config.client.listener;

import io.lighting.config.core.dto.ConfigChange;

@FunctionalInterface
public interface ConfigListener {
    void onChange(ConfigChange change);
}
