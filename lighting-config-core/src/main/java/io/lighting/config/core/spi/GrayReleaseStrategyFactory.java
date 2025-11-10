package io.lighting.config.core.spi;

import io.lighting.config.core.api.GrayReleaseStrategy;

import java.util.Map;

/**
 * Factory for gray release strategies so they can be registered via ServiceLoader.
 */
public interface GrayReleaseStrategyFactory {

    boolean supports(String name);

    GrayReleaseStrategy create(Map<String, String> options);
}
