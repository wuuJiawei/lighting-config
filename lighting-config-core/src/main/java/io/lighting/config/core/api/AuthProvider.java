package io.lighting.config.core.api;

/**
 * SPI for authenticating and authorizing client operations.
 */
public interface AuthProvider extends AutoCloseable {

    AuthResult authenticate(AuthRequest request);

    default boolean supports(String scheme) {
        return true;
    }

    @Override
    default void close() {
        // no-op
    }
}
