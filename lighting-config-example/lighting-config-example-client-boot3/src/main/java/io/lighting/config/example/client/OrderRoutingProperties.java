package io.lighting.config.example.client;

import io.lighting.config.spring.boot.annotation.LightingProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Example bean showing how {@link LightingProperties} can hydrate complex objects.
 */
@SuppressWarnings({ "FieldCanBeLocal", "FieldMayBeFinal" })
@Component
@LightingProperties(prefix = "order.routing")
public class OrderRoutingProperties {

    private boolean enabled = true;
    private Duration slowThreshold = Duration.ofSeconds(2);
    private int workerPoolSize = 4;
    private List<String> preferredRegions = List.of("ap-shanghai", "ap-singapore");
    private Map<String, Integer> regionWeights = Map.of("ap-shanghai", 70, "ap-singapore", 30);

    public boolean isEnabled() {
        return enabled;
    }

    public Duration getSlowThreshold() {
        return slowThreshold;
    }

    public int getWorkerPoolSize() {
        return workerPoolSize;
    }

    public List<String> getPreferredRegions() {
        return preferredRegions;
    }

    public Map<String, Integer> getRegionWeights() {
        return regionWeights;
    }
}
