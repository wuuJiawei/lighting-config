package io.lighting.config.core.api;

/**
 * Determines whether a specific config item change should reach a client.
 */
public interface GrayReleaseStrategy {

    boolean matches(GrayReleaseContext context);

    default String name() {
        return getClass().getSimpleName();
    }
}
