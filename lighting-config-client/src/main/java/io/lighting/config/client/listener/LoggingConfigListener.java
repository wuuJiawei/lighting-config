package io.lighting.config.client.listener;

import io.lighting.config.core.dto.ConfigChange;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

/**
 * Built-in listener that logs every config change with before/after metadata.
 */
public final class LoggingConfigListener implements ConfigListener {

    private final Logger logger;
    private final Map<String, Snapshot> lastSeen = new ConcurrentHashMap<>();

    public LoggingConfigListener(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onChange(ConfigChange change) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        String key = change.getCoordinate().getKey();
        Snapshot previous = lastSeen.get(key);
        String prevValue = previous == null ? "<none>" : previous.value();
        String prevVersion = previous == null ? "-" : String.valueOf(previous.version());
        String newValue = change.isDeleted() ? "<deleted>" : change.getValue();
        long occurredAtMillis = change.getOccurredAt() == 0 ? System.currentTimeMillis() : change.getOccurredAt();
        Instant occurredAt = Instant.ofEpochMilli(occurredAtMillis);

        logger.info(
                "config change applied | tenant={} | namespace={} | appId={} | key={} | type={} | prevVersion={} | newVersion={} | prevValue={} | newValue={} | contentType={} | occurredAt={}",
                change.getCoordinate().getTenant(),
                change.getCoordinate().getNamespace(),
                change.getCoordinate().getAppId(),
                key,
                change.getType(),
                prevVersion,
                change.getVersion(),
                prevValue,
                newValue,
                change.getContentType().name(),
                occurredAt);

        if (change.isDeleted()) {
            lastSeen.remove(key);
        } else {
            lastSeen.put(key, new Snapshot(change.getValue(), change.getVersion(), change.getContentType().name()));
        }
    }

    private static final class Snapshot {
        private final String value;
        private final long version;
        private final String contentType;

        private Snapshot(String value, long version, String contentType) {
            this.value = value;
            this.version = version;
            this.contentType = contentType;
        }

        private String value() {
            return value;
        }

        private long version() {
            return version;
        }

        @SuppressWarnings("unused")
        private String contentType() {
            return contentType;
        }
    }
}
