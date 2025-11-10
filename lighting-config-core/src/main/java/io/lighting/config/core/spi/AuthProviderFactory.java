package io.lighting.config.core.spi;

import io.lighting.config.core.api.AuthProvider;

import java.util.Map;

/**
 * Allows pluggable AuthProvider implementations with externalized configuration.
 */
public interface AuthProviderFactory {

    boolean supports(String mode);

    AuthProvider create(Map<String, String> options);
}
