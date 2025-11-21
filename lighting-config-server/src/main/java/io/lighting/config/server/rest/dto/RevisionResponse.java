package io.lighting.config.server.rest.dto;

import io.lighting.config.core.model.Revision;
import io.lighting.config.core.model.RevisionOperation;

import java.time.Instant;

public class RevisionResponse {
    private String tenant;
    private String namespace;
    private String appId;
    private String key;
    private long version;
    private RevisionOperation op;
    private String operator;
    private String diff;
    private Instant createdAt;

    public RevisionResponse(Revision revision) {
        this.tenant = revision.getCoordinate().getTenant();
        this.namespace = revision.getCoordinate().getNamespace();
        this.appId = revision.getCoordinate().getAppId();
        this.key = revision.getCoordinate().getKey();
        this.version = revision.getVersion();
        this.op = revision.getOperation();
        this.operator = revision.getOperator();
        this.diff = revision.getDiff();
        this.createdAt = revision.getCreatedAt();
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

    public long getVersion() {
        return version;
    }

    public RevisionOperation getOp() {
        return op;
    }

    public String getOperator() {
        return operator;
    }

    public String getDiff() {
        return diff;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
