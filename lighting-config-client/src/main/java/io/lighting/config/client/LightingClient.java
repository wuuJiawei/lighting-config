package io.lighting.config.client;

import io.lighting.config.client.cache.ConfigCache;
import io.lighting.config.client.config.ClientOptions;
import io.lighting.config.client.listener.ConfigListener;
import io.lighting.config.client.listener.ListenerRegistry;
import io.lighting.config.client.transport.PollingTransport;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PollAdvice;
import io.lighting.config.core.dto.PollRequest;
import io.lighting.config.core.dto.PollResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for interacting with the config server using periodic polling.
 */
public class LightingClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(LightingClient.class);
    private static final String BANNER =
            "  _      _ _   _ _   _ _   _ _____ _____ _   _  _____ _____\n"
                    + " | |    (_) | (_) | (_) \\ | |_   _|  __ \\ \\ | |/ ____|  __ \\\n"
                    + " | |     _| |_ _| |_ _|  \\| | | | | |  | |\\| | |    | |__) |\n"
                    + " | |    | | __| | __| | . ` | | | | |  | | . ` | |    |  _  /\n"
                    + " | |____| | |_ | | |_ | |\\  |_| |_| |__| | |\\  | |____| | \\ \\\n"
                    + " |______|_|\\__|/ |\\__|_| \\_|_____|_____/|_| \\_|\\_____|_|  \\_\\\n"
                    + "              _/ |\n"
                    + "             |__/";

    private final ClientOptions options;
    private final PollingTransport transport;
    private final ConfigCache cache = new ConfigCache();
    private final ListenerRegistry listenerRegistry = new ListenerRegistry();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicLong lastVersion = new AtomicLong(0);
    private final ExecutorService notifier = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "lighting-listener");
        t.setDaemon(true);
        return t;
    });
    private final ScheduledExecutorService pollScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "lighting-poller");
        t.setDaemon(true);
        return t;
    });
    private ScheduledFuture<?> scheduledPoll;

    public LightingClient(ClientOptions options,
                          PollingTransport transport) {
        this.options = options;
        this.transport = transport;
    }

    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        maybePrintBanner();
        logStartupLine("initializing");
        scheduleNextPoll(true, Duration.ZERO);
    }

    private void scheduleNextPoll(boolean bootstrap, Duration delay) {
        scheduledPoll = pollScheduler.schedule(() -> pollOnce(bootstrap),
                Math.max(0, delay.toMillis()),
                TimeUnit.MILLISECONDS);
    }

    private void pollOnce(boolean bootstrap) {
        try {
            PollRequest request = PollRequest.builder()
                    .tenant(options.getTenant())
                    .namespace(options.getNamespace())
                    .appId(options.getAppId())
                    .labels(options.getLabels())
                    .metadata(options.getMetadata())
                    .lastVersion(bootstrap ? 0 : lastVersion.get())
                    .prefixes(options.getBootstrapPrefixes())
                    .clientTime(System.currentTimeMillis())
                    .build();
            PollResponse response = transport.poll(request);
            if (!response.getItems().isEmpty() && response.getVersion() > lastVersion.get()) {
                response.getItems().forEach(this::applyChange);
                lastVersion.updateAndGet(current -> Math.max(current, response.getVersion()));
                logStartupLine(bootstrap ? "started" : "synced");
            } else if (bootstrap) {
                logStartupLine("started");
            }
            scheduleNextPoll(false, resolveNextInterval(response.getAdvice()));
        } catch (Exception ex) {
            log.warn("Polling request failed: {}", ex.getMessage());
            scheduleNextPoll(false, options.getPollInterval());
        }
    }

    private Duration resolveNextInterval(PollAdvice advice) {
        if (advice == null || advice.getNextInterval() == null || advice.getNextInterval().isZero()) {
            return options.getPollInterval();
        }
        return advice.getNextInterval();
    }

    private void applyChange(ConfigChange change) {
        cache.apply(change);
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
        if (scheduledPoll != null) {
            scheduledPoll.cancel(true);
        }
        pollScheduler.shutdownNow();
        try {
            transport.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close transport", e);
        } finally {
            notifier.shutdownNow();
        }
        log.info("lighting-config client stopped | appId={}", options.getAppId());
    }

    private void maybePrintBanner() {
        if (options.isBannerEnabled()) {
            log.info("\n{}", BANNER);
        }
    }

    private void logStartupLine(String state) {
        log.info(
                "lighting-config client {} | version={} | tenant={} | namespace={} | appId={} | transport={}",
                state,
                resolveVersion(),
                options.getTenant(),
                options.getNamespace(),
                options.getAppId(),
                transport.getClass().getSimpleName());
    }

    private String resolveVersion() {
        Package pkg = LightingClient.class.getPackage();
        if (pkg != null) {
            String version = pkg.getImplementationVersion();
            if (version != null && !version.isBlank()) {
                return version;
            }
        }
        return "dev";
    }
}
