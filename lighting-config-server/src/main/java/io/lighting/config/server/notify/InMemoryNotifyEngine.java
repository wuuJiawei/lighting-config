package io.lighting.config.server.notify;

import io.lighting.config.core.dto.ConfigChangeEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryNotifyEngine implements NotifyEngine {

    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();

    @Override
    public Registration register(Subscriber subscriber) {
        subscribers.add(subscriber);
        return () -> subscribers.remove(subscriber);
    }

    @Override
    public void publish(ConfigChangeEvent event) {
        for (Subscriber subscriber : subscribers) {
            if (subscriber.matches(event)) {
                subscriber.onEvent(event);
            }
        }
    }
}
