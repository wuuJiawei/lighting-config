package io.lighting.config.client;

import io.lighting.config.client.config.ClientOptions;
import io.lighting.config.client.transport.PollingTransport;
import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PollAdvice;
import io.lighting.config.core.dto.PollRequest;
import io.lighting.config.core.dto.PollResponse;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ContentType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LightingClientTest {

    private StubTransport transport;
    private LightingClient client;

    @BeforeEach
    void setUp() {
        transport = new StubTransport();
        transport.enqueue(upsert("alpha", "v1", 1));
        ClientOptions options = ClientOptions.builder()
                .serverAddress("http://localhost:8080")
                .tenant("tenant")
                .namespace("ns")
                .appId("app")
                .pollInterval(Duration.ofMillis(50))
                .build();
        client = new LightingClient(options, transport);
        client.start();
    }

    @AfterEach
    void tearDown() {
        client.close();
    }

    @Test
    void bootstrapLoadsCache() throws InterruptedException {
        assertTrue(waitForValue("alpha", "v1"));
    }

    @Test
    void listenerGetsUpdatesFromPoll() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ConfigChange> received = new AtomicReference<>();
        client.addListener("feature.", change -> {
            received.set(change);
            latch.countDown();
        });

        transport.enqueue(upsert("feature.toggle", "true", 2));
        assertTrue(latch.await(1, TimeUnit.SECONDS), "listener should be invoked");
        assertEquals("true", received.get().getValue());
        assertTrue(waitForValue("feature.toggle", "true"));
    }

    private boolean waitForValue(String key, String expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 1_000;
        while (System.currentTimeMillis() < deadline) {
            Optional<String> value = client.get(key);
            if (value.isPresent() && expected.equals(value.get())) {
                return true;
            }
            Thread.sleep(20);
        }
        return false;
    }

    private static ConfigChange upsert(String key, String value, long version) {
        return ConfigChange.builder()
                .coordinate(ConfigCoordinate.of("tenant", "ns", "app", key))
                .version(version)
                .type(ChangeType.UPSERT)
                .contentType(ContentType.TEXT)
                .value(value)
                .occurredAt(System.currentTimeMillis())
                .build();
    }

    private static final class StubTransport implements PollingTransport {

        private final List<PollResponse> responses = new ArrayList<>();
        private int index = 0;

        void enqueue(ConfigChange change) {
            responses.add(PollResponse.builder()
                    .version(change.getVersion())
                    .items(List.of(change))
                    .advice(PollAdvice.builder().nextInterval(Duration.ofMillis(50)).build())
                    .build());
        }

        @Override
        public PollResponse poll(PollRequest request) {
            if (index < responses.size()) {
                return responses.get(index++);
            }
            return PollResponse.builder()
                    .version(request.getLastVersion())
                    .items(List.of())
                    .advice(PollAdvice.builder().nextInterval(Duration.ofMillis(50)).build())
                    .build();
        }
    }
}
