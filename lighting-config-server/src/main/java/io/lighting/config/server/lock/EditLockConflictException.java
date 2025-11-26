package io.lighting.config.server.lock;

public class EditLockConflictException extends RuntimeException {

    private final ConfigEditLock lock;

    public EditLockConflictException(ConfigEditLock lock) {
        super("Configuration is being edited by another session");
        this.lock = lock;
    }

    public ConfigEditLock getLock() {
        return lock;
    }
}
