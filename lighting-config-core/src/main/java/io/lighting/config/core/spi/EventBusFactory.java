package io.lighting.config.core.spi;

import io.lighting.config.core.api.EventBus;

import java.util.Map;

/**
 * SPI for customizing the event bus implementation (e.g. Guava, Reactor, custom).
 */
public interface EventBusFactory {

    EventBus create(Map<String, String> options);
}
