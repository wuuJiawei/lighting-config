package io.lighting.config.server.lock;

import java.util.Objects;

public class LockOwner {

    private final String id;
    private final String name;

    public LockOwner(String id, String name) {
        this.id = Objects.requireNonNull(id, "owner id must not be null");
        this.name = name == null ? "" : name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String fingerprint() {
        return id.length() <= 8 ? id : id.substring(0, 8);
    }
}
