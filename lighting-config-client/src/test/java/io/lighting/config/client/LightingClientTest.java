package io.lighting.config.client;

import io.lighting.config.client.config.ClientOptions;
import io.lighting.config.client.transport.ConfigTransport;
import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class LightingClientTest {

    private StubTransport transport;
    private LightingClient client;

    @BeforeEach
    void setUp() {
        transport = new StubTransport();
        transport.setBootstrap(List.of(sampleItem("alpha", "v1", 1)));
        ClientOptions options = ClientOptions.builder()
                .serverAddress("in-memory")
                .tenant("tenant")
                .namespace("ns")
                .appId("app")
                .build();
        client = new LightingClient(options, transport);
        client.start();
    }

    @AfterEach
    void tearDown() {
        client.close();
    }

    @Test
    void bootstrapLoadsCache() {
        Optional<String> value = client.get("alpha");
        assertTrue(value.isPresent());
        assertEquals("v1", value.get());
    }

    @Test
    void listenerGetsUpdatesFromWatch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ConfigChange> received = new AtomicReference<>();
        client.addListener("feature.", change -> {
            received.set(change);
            latch.countDown();
        });

        ConfigChange change = ConfigChange.builder()
                .coordinate(ConfigCoordinate.of("tenant", "ns", "app", "feature.toggle"))
                .version(2)
                .type(ChangeType.UPSERT)
                .contentType(ContentType.TEXT)
                .value("true")
                .occurredAt(Instant.now())
                .build();
        transport.emit(change);

        assertTrue(latch.await(1, TimeUnit.SECONDS), "listener should be invoked");
        assertEquals("true", received.get().getValue());
        assertEquals("true", client.get("feature.toggle").orElse(null));
    }

    private static ConfigItem sampleItem(String key, String value, long version) {
        return ConfigItem.builder()
                .tenant("tenant")
                .namespace("ns")
                .appId("app")
                .key(key)
                .value(value)
                .contentType(ContentType.TEXT)
                .version(version)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private static final class StubTransport implements ConfigTransport {
        private List<ConfigItem> bootstrap = List.of();
        private Consumer<ConfigChange> consumer;

        void setBootstrap(List<ConfigItem> items) {
            this.bootstrap = items;
        }

        void emit(ConfigChange change) {
            if (consumer != null) {
                consumer.accept(change);
            }
        }

        @Override
        public List<ConfigItem> pull(io.lighting.config.core.dto.PullQuery query) {
            return bootstrap;
        }

        @Override
        public WatchHandle watch(io.lighting.config.core.dto.WatchRequest request, java.util.function.Consumer<ConfigChange> consumer) {
            this.consumer = consumer;
            return () -> {};
        }

        @Override
        public void close() {
        }
    }
}
