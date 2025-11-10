package io.lighting.config.spring.boot;

import io.lighting.config.client.LightingClient;
import io.lighting.config.client.transport.ConfigTransport;
import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
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

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LightingClientAutoConfigurationTest.TestConfiguration.class, properties = {
        "lighting.client.server.address=dns:///localhost:9090"
})
class LightingClientAutoConfigurationTest {

    @Autowired
    private LightingClient client;

    @Autowired
    private StubConfigTransport transport;

    @Autowired
    private TestBean testBean;

    @Autowired
    private FeatureProperties featureProperties;

    @Test
    void lightingValueInjectedFromClient() {
        assertTrue(testBean.featureFlag);
        assertTrue(featureProperties.flagEnabled);
    }

    @Test
    void listenersAreTriggered() throws InterruptedException {
        ConfigChange change = ConfigChange.builder()
                .coordinate(ConfigCoordinate.of("default", "default", "default", "feature.mode"))
                .version(2)
                .type(ChangeType.UPSERT)
                .contentType(ContentType.TEXT)
                .value("on")
                .occurredAt(Instant.now())
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
        StubConfigTransport stubConfigTransport() {
            StubConfigTransport transport = new StubConfigTransport();
            transport.setBootstrap(List.of(
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

    static class StubConfigTransport implements ConfigTransport {
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
