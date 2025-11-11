package io.lighting.config.embedded;

import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.spring.boot.annotation.LightingValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LightingEmbeddedAutoConfigurationTest.TestApplication.class, properties = {
        "lighting.config.mode=embedded",
        "server.port=0",
        "lighting.config.client.auto-start=false",
        "lighting.config.client.server.address=http://localhost:7086"
})
class LightingEmbeddedAutoConfigurationTest {

    @Autowired
    private ConfigRepository repository;

    @Autowired
    private SampleBean bean;

    @Test
    void repositoryIsEmbedded() {
        assertEquals("io.lighting.config.embedded.InMemoryEmbeddedRepository", repository.getClass().getName());
    }

    @Test
    void lightingValueInjected() {
        assertFalse(bean.flag);
        bean.flag = true; // ensure bean instantiated
    }

    @Configuration
    @EnableLightingEmbedded
    static class TestApplication {
        @Bean
        SampleBean sampleBean() {
            return new SampleBean();
        }
    }

    static class SampleBean {
        @LightingValue(key = "feature.flag", defaultValue = "false")
        private boolean flag;
    }
}
