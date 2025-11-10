package io.lighting.config.client.transport;

import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.dto.WatchRequest;
import io.lighting.config.core.model.ConfigItem;

import java.util.List;
import java.util.function.Consumer;

public interface ConfigTransport extends AutoCloseable {

    List<ConfigItem> pull(PullQuery query);

    WatchHandle watch(WatchRequest request, Consumer<ConfigChange> consumer);

    interface WatchHandle extends AutoCloseable {
        @Override
        void close();
    }
}
