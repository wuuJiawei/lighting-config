package io.lighting.config.example.client;

import io.lighting.config.spring.boot.annotation.LightingListener;
import io.lighting.config.spring.boot.annotation.LightingValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicReference;

@RestController
public class FeatureToggleController {

    @LightingValue(key = "feature.order.v2", defaultValue = "false")
    private boolean orderV2;

    private final AtomicReference<String> lastEvent = new AtomicReference<>("none");

    @GetMapping("/feature/order")
    public String orderFlag() {
        return Boolean.toString(orderV2);
    }

    @LightingListener(prefix = "feature.")
    public void onFeatureChange(io.lighting.config.core.dto.ConfigChange change) {
        lastEvent.set(change.getCoordinate().getKey() + "=" + change.getValue());
    }

    @GetMapping("/events/last")
    public String lastEvent() {
        return lastEvent.get();
    }
}
