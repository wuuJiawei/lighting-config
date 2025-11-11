package io.lighting.config.client.transport;

import io.lighting.config.core.dto.PollRequest;
import io.lighting.config.core.dto.PollResponse;

/**
 * Abstraction over the communication mechanism used by {@link io.lighting.config.client.LightingClient}
 * to retrieve configuration changes through polling.
 */
public interface PollingTransport extends AutoCloseable {

    PollResponse poll(PollRequest request);

    @Override
    default void close() {
    }
}
