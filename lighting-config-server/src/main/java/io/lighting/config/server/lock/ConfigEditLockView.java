package io.lighting.config.server.lock;

import io.lighting.config.core.model.ConfigCoordinate;

import java.time.Instant;

public class ConfigEditLockView {

    private String tenant;
    private String namespace;
    private String appId;
    private String key;
    private boolean locked;
    private boolean ownedByMe;
    private String ownerName;
    private String ownerFingerprint;
    private Instant expiresAt;
    private String event;

    public static ConfigEditLockView unlocked(ConfigCoordinate coordinate, LockOwner requester, LockEventType type) {
        ConfigEditLockView view = new ConfigEditLockView();
        view.tenant = coordinate.getTenant();
        view.namespace = coordinate.getNamespace();
        view.appId = coordinate.getAppId();
        view.key = coordinate.getKey();
        view.locked = false;
        view.ownedByMe = false;
        view.ownerFingerprint = "";
        view.ownerName = "";
        view.expiresAt = null;
        view.event = type.name();
        return view;
    }

    public static ConfigEditLockView from(ConfigEditLock lock, LockOwner requester, LockEventType type) {
        ConfigEditLockView view = new ConfigEditLockView();
        view.tenant = lock.getCoordinate().getTenant();
        view.namespace = lock.getCoordinate().getNamespace();
        view.appId = lock.getCoordinate().getAppId();
        view.key = lock.getCoordinate().getKey();
        view.locked = type != LockEventType.RELEASED && type != LockEventType.EXPIRED;
        view.ownedByMe = view.locked && lock.isOwnedBy(requester.getId());
        view.ownerName = lock.getOwnerName();
        view.ownerFingerprint = lock.ownerFingerprint();
        view.expiresAt = lock.getExpiresAt();
        view.event = type.name();
        return view;
    }

    public String getTenant() {
        return tenant;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getAppId() {
        return appId;
    }

    public String getKey() {
        return key;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isOwnedByMe() {
        return ownedByMe;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOwnerFingerprint() {
        return ownerFingerprint;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getEvent() {
        return event;
    }
}
