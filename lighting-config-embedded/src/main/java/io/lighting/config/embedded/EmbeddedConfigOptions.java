package io.lighting.config.embedded;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

/**
 * Configuration holder for embedded mode.
 */
public final class EmbeddedConfigOptions {

    public enum StorageType {
        MEMORY,
        FILE
    }

    private final StorageType storageType;
    private final Path storagePath;
    private final Duration flushInterval;

    private EmbeddedConfigOptions(Builder builder) {
        this.storageType = builder.storageType;
        this.storagePath = builder.storagePath;
        this.flushInterval = builder.flushInterval;
    }

    public static Builder builder() {
        return new Builder();
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public Path getStoragePath() {
        return storagePath;
    }

    public Duration getFlushInterval() {
        return flushInterval;
    }

    public static final class Builder {
        private StorageType storageType = StorageType.MEMORY;
        private Path storagePath;
        private Duration flushInterval = Duration.ofSeconds(5);

        public Builder storageType(StorageType storageType) {
            this.storageType = Objects.requireNonNull(storageType, "storageType");
            return this;
        }

        public Builder storagePath(Path storagePath) {
            this.storagePath = storagePath;
            return this;
        }

        public Builder flushInterval(Duration flushInterval) {
            if (flushInterval != null) {
                this.flushInterval = flushInterval;
            }
            return this;
        }

        public EmbeddedConfigOptions build() {
            if (storageType == StorageType.FILE && storagePath == null) {
                throw new IllegalStateException("storagePath must be specified when using file storage");
            }
            return new EmbeddedConfigOptions(this);
        }
    }
}
