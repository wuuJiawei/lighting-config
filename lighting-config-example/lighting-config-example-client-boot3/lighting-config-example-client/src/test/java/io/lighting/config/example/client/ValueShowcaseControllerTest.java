package io.lighting.config.example.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ValueShowcaseController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration.class,
        org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class,
        org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration.class,
        org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class,
        org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration.class
})
@Import(ValueShowcaseControllerTest.TestConfig.class)
class ValueShowcaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ValueShowcaseController controller;

    @Autowired
    private OrderRoutingProperties routingProperties;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "maxRetries", 5);
        ReflectionTestUtils.setField(controller, "healthThreshold", 0.82d);
        ReflectionTestUtils.setField(controller, "executionMode", ValueShowcaseController.ExecutionMode.PARALLEL);
        ReflectionTestUtils.setField(controller, "baseDelay", Duration.ofSeconds(4));
        ReflectionTestUtils.setField(controller, "supportedCurrencies", List.of("CNY", "USD", "SGD"));
        ReflectionTestUtils.setField(controller, "discountPerTier", Map.of("silver", 3, "gold", 7));

        ReflectionTestUtils.setField(routingProperties, "enabled", false);
        ReflectionTestUtils.setField(routingProperties, "slowThreshold", Duration.ofSeconds(8));
        ReflectionTestUtils.setField(routingProperties, "workerPoolSize", 16);
        ReflectionTestUtils.setField(routingProperties, "preferredRegions", List.of("ap-singapore"));
        ReflectionTestUtils.setField(routingProperties, "regionWeights", Map.of("ap-singapore", 90));
    }

    @Test
    void lightingValuesEndpointExposesTypedValues() throws Exception {
        mockMvc.perform(get("/examples/lighting-values"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxRetries").value(5))
                .andExpect(jsonPath("$.healthThreshold").value(0.82))
                .andExpect(jsonPath("$.executionMode").value("PARALLEL"))
                .andExpect(jsonPath("$.baseDelay").value("PT4S"))
                .andExpect(jsonPath("$.supportedCurrencies[0]").value("CNY"))
                .andExpect(jsonPath("$.discountPerTier.gold").value(7));
    }

    @Test
    void orderRoutingPropertiesEndpointReflectsBeanFields() throws Exception {
        mockMvc.perform(get("/examples/lighting-properties/order-routing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.slowThreshold").value("PT8S"))
                .andExpect(jsonPath("$.workerPoolSize").value(16))
                .andExpect(jsonPath("$.preferredRegions[0]").value("ap-singapore"))
                .andExpect(jsonPath("$.regionWeights['ap-singapore']").value(90));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        OrderRoutingProperties routingProperties() {
            // Provide a plain bean so @LightingProperties binding can be simulated via ReflectionTestUtils.
            return new OrderRoutingProperties();
        }
    }
}
