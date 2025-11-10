package io.lighting.config.client.listener;

import io.lighting.config.core.dto.ConfigChange;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListenerRegistry {

    private final List<RegisteredListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(String prefix, ConfigListener listener) {
        listeners.add(new RegisteredListener(prefix, listener));
    }

    public void notifyListeners(ConfigChange change) {
        String key = change.getCoordinate().getKey();
        for (RegisteredListener listener : listeners) {
            if (listener.matches(key)) {
                listener.listener.onChange(change);
            }
        }
    }

    private static final class RegisteredListener {
        private final String prefix;
        private final ConfigListener listener;

        private RegisteredListener(String prefix, ConfigListener listener) {
            this.prefix = prefix;
            this.listener = listener;
        }

        private boolean matches(String key) {
            return prefix == null || prefix.isEmpty() || key.startsWith(prefix);
        }
    }
}
