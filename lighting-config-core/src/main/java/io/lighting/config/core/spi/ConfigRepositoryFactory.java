package io.lighting.config.core.spi;

import io.lighting.config.core.api.ConfigRepository;

/**
 * Service loader entry point for providing ConfigRepository implementations.
 */
public interface ConfigRepositoryFactory {

    boolean supports(StorageType storageType);

    ConfigRepository create(StorageProviderContext context);
}
