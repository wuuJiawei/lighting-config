package io.lighting.config.server.notify;

import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PollRequest;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryChangeFeed implements ChangeFeed {

    private static final int DEFAULT_CAPACITY = 10_000;

    private final Deque<Entry> buffer = new ConcurrentLinkedDeque<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final int capacity;

    public InMemoryChangeFeed() {
        this(DEFAULT_CAPACITY);
    }

    public InMemoryChangeFeed(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public long append(ConfigChange change) {
        long next = sequence.incrementAndGet();
        buffer.addLast(new Entry(next, change));
        while (buffer.size() > capacity) {
            buffer.pollFirst();
        }
        return next;
    }

    @Override
    public Batch fetchSince(PollRequest request, long lastVersion) {
        List<ConfigChange> changes = new ArrayList<>();
        long latest = lastVersion;
        for (Entry entry : buffer) {
            if (entry.sequence <= lastVersion) {
                continue;
            }
            latest = Math.max(latest, entry.sequence);
            if (matches(request, entry.change)) {
                changes.add(entry.change);
            }
        }
        return new Batch(changes, latest);
    }

    @Override
    public long currentOffset() {
        return sequence.get();
    }

    private boolean matches(PollRequest request, ConfigChange change) {
        if (!change.getCoordinate().getTenant().equals(request.getTenant())) {
            return false;
        }
        if (!change.getCoordinate().getNamespace().equals(request.getNamespace())) {
            return false;
        }
        if (!change.getCoordinate().getAppId().equals(request.getAppId())) {
            return false;
        }
        if (request.getPrefixes().isEmpty()) {
            return true;
        }
        String key = change.getCoordinate().getKey();
        for (String prefix : request.getPrefixes()) {
            if (key.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static final class Entry {
        private final long sequence;
        private final ConfigChange change;

        private Entry(long sequence, ConfigChange change) {
            this.sequence = sequence;
            this.change = change;
        }
    }
}
