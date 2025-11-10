package io.lighting.config.client;

import io.lighting.config.client.cache.ConfigCache;
import io.lighting.config.client.config.ClientOptions;
import io.lighting.config.client.listener.ConfigListener;
import io.lighting.config.client.listener.ListenerRegistry;
import io.lighting.config.client.transport.ConfigTransport;
import io.lighting.config.client.transport.ConfigTransport.WatchHandle;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.ConfigSelector;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.dto.WatchRequest;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.LabelSet;
import io.lighting.config.core.dto.ClientMetadata;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Main entry point for interacting with the config server.
 */
public class LightingClient implements AutoCloseable {

    private final ClientOptions options;
    private final ConfigTransport transport;
    private final ConfigCache cache;
    private final ListenerRegistry listenerRegistry;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicLong lastVersion = new AtomicLong(0);
    private final ExecutorService notifier = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "lighting-listener");
        t.setDaemon(true);
        return t;
    });
    private WatchHandle watchHandle;

    public LightingClient(ClientOptions options,
                          ConfigTransport transport) {
        this.options = options;
        this.transport = transport;
        this.cache = new ConfigCache();
        this.listenerRegistry = new ListenerRegistry();
    }

    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        bootstrap();
        startWatch();
    }

    private void bootstrap() {
        List<String> prefixes = options.getBootstrapPrefixes();
        if (prefixes.isEmpty()) {
            loadPrefix(null);
        } else {
            prefixes.forEach(this::loadPrefix);
        }
    }

    private void loadPrefix(String prefix) {
        PullQuery.Builder builder = PullQuery.builder()
                .tenant(options.getTenant())
                .namespace(options.getNamespace())
                .appId(options.getAppId());
        if (prefix != null) {
            builder.selector(ConfigSelector.byPrefix(prefix));
        }
        List<ConfigItem> items = transport.pull(builder.build());
        for (ConfigItem item : items) {
            cache.put(item);
            lastVersion.updateAndGet(current -> Math.max(current, item.getVersion()));
        }
    }

    private void startWatch() {
        WatchRequest request = WatchRequest.builder()
                .tenant(options.getTenant())
                .namespace(options.getNamespace())
                .appId(options.getAppId())
                .labels(LabelSet.of(options.getLabels()))
                .client(ClientMetadata.builder()
                        .clientId(options.getMetadata().getOrDefault("clientId", options.getAppId()))
                        .attributes(options.getMetadata())
                        .build())
                .lastKnownVersion(lastVersion.get())
                .build();
        watchHandle = transport.watch(request, this::handleChange);
    }

    private void handleChange(ConfigChange change) {
        cache.apply(change);
        lastVersion.updateAndGet(current -> Math.max(current, change.getVersion()));
        notifier.submit(() -> listenerRegistry.notifyListeners(change));
    }

    public Optional<String> get(String key) {
        return cache.get(key).map(ConfigCache.Snapshot::value);
    }

    public Optional<ConfigCache.Snapshot> getSnapshot(String key) {
        return cache.get(key);
    }

    public void addListener(String prefix, ConfigListener listener) {
        listenerRegistry.addListener(prefix, listener);
    }

    @Override
    public void close() {
        if (watchHandle != null) {
            watchHandle.close();
        }
        try {
            transport.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close transport", e);
        } finally {
            notifier.shutdownNow();
        }
    }
}
