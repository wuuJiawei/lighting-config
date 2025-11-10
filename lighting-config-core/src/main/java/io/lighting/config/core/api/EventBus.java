package io.lighting.config.core.api;

import java.util.function.Consumer;

/**
 * Minimal event bus abstraction for cross-module communication.
 */
public interface EventBus {

    <E> Subscription subscribe(Class<E> eventType, Consumer<E> consumer);

    void publish(Object event);

    interface Subscription extends AutoCloseable {
        @Override
        void close();
    }
}
