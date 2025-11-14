package io.lighting.config.server.rest.dto;

import io.lighting.config.server.monitoring.CacheMissAlert;

import java.time.Instant;

public class CacheMissAlertResponse {

    private final String namespace;
    private final String appId;
    private final String selector;
    private final int missCount;
    private final Instant occurredAt;

    public CacheMissAlertResponse(CacheMissAlert alert) {
        this.namespace = alert.getNamespace();
        this.appId = alert.getAppId();
        this.selector = alert.getSelector();
        this.missCount = alert.getMissCount();
        this.occurredAt = alert.getOccurredAt();
    }

    public String getNamespace() {
        return namespace;
    }

    public String getAppId() {
        return appId;
    }

    public String getSelector() {
        return selector;
    }

    public int getMissCount() {
        return missCount;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
