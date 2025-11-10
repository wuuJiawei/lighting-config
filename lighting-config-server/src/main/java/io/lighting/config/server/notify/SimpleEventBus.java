package io.lighting.config.server.notify;

import io.lighting.config.core.api.EventBus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class SimpleEventBus implements EventBus {

    private final Map<Class<?>, List<Consumer<?>>> consumers = new ConcurrentHashMap<>();

    @Override
    public <E> Subscription subscribe(Class<E> eventType, Consumer<E> consumer) {
        List<Consumer<?>> list = consumers.computeIfAbsent(eventType, key -> new CopyOnWriteArrayList<>());
        list.add(consumer);
        return () -> list.remove(consumer);
    }

    @Override
    public void publish(Object event) {
        if (event == null) {
            return;
        }
        Class<?> type = event.getClass();
        consumers.forEach((registeredType, handlers) -> {
            if (registeredType.isAssignableFrom(type)) {
                for (Consumer<?> handler : handlers) {
                    @SuppressWarnings("unchecked")
                    Consumer<Object> consumer = (Consumer<Object>) handler;
                    consumer.accept(event);
                }
            }
        });
    }
}
