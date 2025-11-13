package io.lighting.config.server.rest.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NamespaceSummaryResponse {

    private final String id;
    private final String name;
    private final String owner;
    private final long configCount;
    private final long watchers;
    private final List<String> appIds;
    private final Instant updatedAt;

    public NamespaceSummaryResponse(String name,
                                    String owner,
                                    long configCount,
                                    long watchers,
                                    List<String> appIds,
                                    Instant updatedAt) {
        this.id = name;
        this.name = name;
        this.owner = owner;
        this.configCount = configCount;
        this.watchers = watchers;
        this.appIds = new ArrayList<>(appIds);
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public long getConfigCount() {
        return configCount;
    }

    public long getWatchers() {
        return watchers;
    }

    public List<String> getAppIds() {
        return Collections.unmodifiableList(appIds);
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
