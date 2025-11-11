package io.lighting.config.server.notify;

import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PollRequest;

import java.util.List;

public interface ChangeFeed {

    long append(ConfigChange change);

    Batch fetchSince(PollRequest request, long lastVersion);

    long currentOffset();

    final class Batch {
        private final List<ConfigChange> changes;
        private final long lastVersion;

        public Batch(List<ConfigChange> changes, long lastVersion) {
            this.changes = changes;
            this.lastVersion = lastVersion;
        }

        public List<ConfigChange> getChanges() {
            return changes;
        }

        public long getLastVersion() {
            return lastVersion;
        }
    }
}
