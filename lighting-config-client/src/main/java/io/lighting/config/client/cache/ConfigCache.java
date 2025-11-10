package io.lighting.config.client.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.model.ConfigItem;

import java.time.Duration;
import java.util.Optional;

public class ConfigCache {

    private final Cache<String, Snapshot> cache;

    public ConfigCache() {
        this(CacheBuilder.defaultBuilder());
    }

    ConfigCache(Caffeine<Object, Object> builder) {
        this.cache = builder.build();
    }

    public Optional<Snapshot> get(String key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    public void put(ConfigItem item) {
        cache.put(item.getKey(), new Snapshot(item.getValue(), item.getContentType().name(), item.getVersion()));
    }

    public void apply(ConfigChange change) {
        if (change.isDeleted()) {
            cache.invalidate(change.getCoordinate().getKey());
        } else {
            cache.put(change.getCoordinate().getKey(),
                    new Snapshot(change.getValue(), change.getContentType().name(), change.getVersion()));
        }
    }

    public static final class Snapshot {
        private final String value;
        private final String contentType;
        private final long version;

        public Snapshot(String value, String contentType, long version) {
            this.value = value;
            this.contentType = contentType;
            this.version = version;
        }

        public String value() {
            return value;
        }

        public String contentType() {
            return contentType;
        }

        public long version() {
            return version;
        }
    }

    static final class CacheBuilder {
        static Caffeine<Object, Object> defaultBuilder() {
            return Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterAccess(Duration.ofMinutes(30));
        }
    }
}
