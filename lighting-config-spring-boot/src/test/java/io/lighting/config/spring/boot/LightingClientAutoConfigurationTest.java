package io.lighting.config.spring.boot;

import io.lighting.config.client.LightingClient;
import io.lighting.config.client.transport.PollingTransport;
import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PollAdvice;
import io.lighting.config.core.dto.PollRequest;
import io.lighting.config.core.dto.PollResponse;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ContentType;
import io.lighting.config.spring.boot.annotation.LightingListener;
import io.lighting.config.spring.boot.annotation.LightingProperties;
import io.lighting.config.spring.boot.annotation.LightingValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LightingClientAutoConfigurationTest.TestConfiguration.class, properties = {
        "lighting.config.client.server.address=http://localhost:8080",
        "lighting.config.client.poll-interval=50ms"
})
class LightingClientAutoConfigurationTest {

    @Autowired
    private LightingClient client;

    @Autowired
    private StubPollingTransport transport;

    @Autowired
    private TestBean testBean;

    @Autowired
    private FeatureProperties featureProperties;

    @Test
    void lightingValueInjectedFromClient() throws InterruptedException {
        assertTrue(waitFor(() -> testBean.featureFlag));
        assertTrue(waitFor(() -> featureProperties.flagEnabled));
    }

    @Test
    void listenersAreTriggered() throws InterruptedException {
        ConfigChange change = ConfigChange.builder()
                .coordinate(ConfigCoordinate.of("default", "default", "default", "feature.mode"))
                .version(2)
                .type(ChangeType.UPSERT)
                .contentType(ContentType.TEXT)
                .value("on")
                .occurredAt(System.currentTimeMillis())
                .build();
        CountDownLatch latch = testBean.expectEvent();
        transport.emit(change);
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals("on", featureProperties.mode);
    }

    private static ConfigItem sampleItem(String key, String value, long version) {
        return ConfigItem.builder()
                .tenant("default")
                .namespace("default")
                .appId("default")
                .key(key)
                .value(value)
                .contentType(ContentType.TEXT)
                .version(version)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Configuration
    @io.lighting.config.spring.boot.annotation.EnableLightingConfig
    static class TestConfiguration {
        @Bean
        @Primary
        StubPollingTransport stubConfigTransport() {
            StubPollingTransport transport = new StubPollingTransport();
            transport.enqueue(List.of(
                    sampleItem("feature.flag", "true", 1),
                    sampleItem("feature.flagEnabled", "true", 1),
                    sampleItem("feature.mode", "off", 1)
            ));
            return transport;
        }

        @Bean
        TestBean lightingTestBean() {
            return new TestBean();
        }

        @Bean
        FeatureProperties featureProperties() {
            return new FeatureProperties();
        }
    }

    static class TestBean {
        @LightingValue(key = "feature.flag", defaultValue = "false")
        private boolean featureFlag;

        private CountDownLatch latch;

        @LightingListener(prefix = "feature.")
        public void onFeatureChange(ConfigChange change) {
            if (latch != null) {
                latch.countDown();
            }
        }

        CountDownLatch expectEvent() {
            latch = new CountDownLatch(1);
            return latch;
        }
    }

    @LightingProperties(prefix = "feature", autoRefresh = true)
    static class FeatureProperties {
        private boolean flagEnabled;
        private String mode;
    }

    private boolean waitFor(BooleanSupplier supplier) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 1_000;
        while (System.currentTimeMillis() < deadline) {
            if (supplier.getAsBoolean()) {
                return true;
            }
            Thread.sleep(20);
        }
        return false;
    }

    static class StubPollingTransport implements PollingTransport {
        private final List<PollResponse> responses = new ArrayList<>();
        private int index = 0;

        void enqueue(List<ConfigItem> items) {
            responses.add(PollResponse.builder()
                    .version(items.stream().mapToLong(ConfigItem::getVersion).max().orElse(0))
                    .items(items.stream().map(StubPollingTransport::toChange).collect(java.util.stream.Collectors.toList()))
                    .advice(PollAdvice.builder().nextInterval(Duration.ofMillis(50)).build())
                    .build());
        }

        void emit(ConfigChange change) {
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

        private static ConfigChange toChange(ConfigItem item) {
            return ConfigChange.builder()
                    .coordinate(ConfigCoordinate.of(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey()))
                    .version(item.getVersion())
                    .type(item.isEnabled() ? ChangeType.UPSERT : ChangeType.DELETE)
                    .contentType(item.getContentType())
                    .value(item.getValue())
                    .deleted(!item.isEnabled())
                    .occurredAt(item.getUpdatedAt().toEpochMilli())
                    .build();
        }
    }
}
