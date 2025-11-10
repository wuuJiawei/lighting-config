package io.lighting.config.server.notify;

import io.lighting.config.core.dto.ConfigChangeEvent;

public interface NotifyEngine {

    Registration register(Subscriber subscriber);

    void publish(ConfigChangeEvent event);

    interface Subscriber {
        boolean matches(ConfigChangeEvent event);

        void onEvent(ConfigChangeEvent event);
    }

    interface Registration extends AutoCloseable {
        @Override
        void close();
    }
}
