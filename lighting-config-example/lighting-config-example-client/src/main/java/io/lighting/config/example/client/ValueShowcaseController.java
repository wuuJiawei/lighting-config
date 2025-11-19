package io.lighting.config.example.client;

import io.lighting.config.spring.boot.annotation.LightingValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates various {@link LightingValue} target types and exposes them via HTTP.
 */
@SuppressWarnings({ "FieldCanBeLocal", "FieldMayBeFinal" })
@RestController
@RequestMapping("/examples")
public class ValueShowcaseController {

    @LightingValue(key = "example.values.max-retries", defaultValue = "3")
    private int maxRetries = 3;

    @LightingValue(key = "example.values.health-threshold", defaultValue = "0.97")
    private double healthThreshold = 0.97d;

    @LightingValue(key = "example.values.execution-mode", defaultValue = "SERIAL")
    private ExecutionMode executionMode = ExecutionMode.SERIAL;

    @LightingValue(key = "example.values.base-delay", defaultValue = "PT2S")
    private Duration baseDelay = Duration.ofSeconds(2);

    @LightingValue(key = "example.values.supported-currencies", defaultValue = "[\"CNY\",\"USD\"]")
    private List<String> supportedCurrencies = List.of("CNY", "USD");

    @LightingValue(key = "example.values.discount-per-tier", defaultValue = "{\"silver\":5,\"gold\":10}")
    private Map<String, Integer> discountPerTier = Map.of("silver", 5, "gold", 10);

    private final OrderRoutingProperties routingProperties;

    public ValueShowcaseController(OrderRoutingProperties routingProperties) {
        this.routingProperties = routingProperties;
    }

    @GetMapping("/lighting-values")
    public Map<String, Object> lightingValues() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("maxRetries", maxRetries);
        payload.put("healthThreshold", healthThreshold);
        payload.put("executionMode", executionMode);
        payload.put("baseDelay", baseDelay);
        payload.put("supportedCurrencies", supportedCurrencies);
        payload.put("discountPerTier", discountPerTier);
        return payload;
    }

    @GetMapping("/lighting-properties/order-routing")
    public OrderRoutingProperties orderRoutingProperties() {
        return routingProperties;
    }

    enum ExecutionMode {
        SERIAL,
        PARALLEL,
        HYBRID
    }
}
