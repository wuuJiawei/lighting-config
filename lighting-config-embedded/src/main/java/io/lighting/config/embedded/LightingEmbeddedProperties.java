package io.lighting.config.embedded;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "lighting.config.embedded")
public class LightingEmbeddedProperties {

    private final Storage storage = new Storage();

    public Storage getStorage() {
        return storage;
    }

    public static class Storage {
        private EmbeddedConfigOptions.StorageType type = EmbeddedConfigOptions.StorageType.MEMORY;
        private Path path;

        public EmbeddedConfigOptions.StorageType getType() {
            return type;
        }

        public void setType(EmbeddedConfigOptions.StorageType type) {
            this.type = type;
        }

        public Path getPath() {
            return path;
        }

        public void setPath(Path path) {
            this.path = path;
        }
    }
}
