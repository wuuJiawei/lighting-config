package io.lighting.config.server.lock;

import io.lighting.config.core.model.ConfigCoordinate;

public class ConfigEditLockEvent {

    private final ConfigCoordinate coordinate;
    private final ConfigEditLock lock;
    private final LockEventType type;

    public ConfigEditLockEvent(ConfigCoordinate coordinate, ConfigEditLock lock, LockEventType type) {
        this.coordinate = coordinate;
        this.lock = lock;
        this.type = type;
    }

    public ConfigCoordinate getCoordinate() {
        return coordinate;
    }

    public ConfigEditLock getLock() {
        return lock;
    }

    public LockEventType getType() {
        return type;
    }
}
