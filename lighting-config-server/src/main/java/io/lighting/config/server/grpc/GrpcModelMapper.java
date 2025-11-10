package io.lighting.config.server.grpc;

import io.lighting.config.core.dto.ConfigChangeEvent;
import io.lighting.config.core.dto.ConfigSelector;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.dto.WatchRequest;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ContentType;
import io.lighting.config.core.model.LabelSet;
import io.lighting.config.link.proto.ConfigUpdate;
import io.lighting.config.link.proto.PullRequest;

public final class GrpcModelMapper {

    private GrpcModelMapper() {
    }

    public static ConfigUpdate toProto(ConfigItem item) {
        return ConfigUpdate.newBuilder()
                .setTenant(item.getTenant())
                .setNamespace(item.getNamespace())
                .setAppId(item.getAppId())
                .setKey(item.getKey())
                .setValue(item.getValue())
                .setContentType(item.getContentType().name())
                .setVersion(item.getVersion())
                .setDeleted(!item.isEnabled())
                .setOccurredAt(item.getUpdatedAt().toEpochMilli())
                .build();
    }

    public static ConfigUpdate toProto(ConfigChangeEvent event) {
        return ConfigUpdate.newBuilder()
                .setTenant(event.getCoordinate().getTenant())
                .setNamespace(event.getCoordinate().getNamespace())
                .setAppId(event.getCoordinate().getAppId())
                .setKey(event.getCoordinate().getKey())
                .setContentType(event.getChange().getContentType().name())
                .setVersion(event.getChange().getVersion())
                .setValue(event.getChange().getValue() == null ? "" : event.getChange().getValue())
                .setDeleted(event.getChange().isDeleted())
                .setOccurredAt(event.getPublishedAt().toEpochMilli())
                .build();
    }

    public static PullQuery toPullQuery(PullRequest request) {
        PullQuery.Builder builder = PullQuery.builder()
                .tenant(request.getTenant())
                .namespace(request.getNamespace())
                .appId(request.getAppId());
        if (request.hasKey()) {
            builder.selector(ConfigSelector.byKey(request.getKey()));
        } else if (request.hasPrefix()) {
            builder.selector(ConfigSelector.byPrefix(request.getPrefix()));
        }
        return builder.build();
    }

    public static WatchRequest toWatchRequest(io.lighting.config.link.proto.WatchRequest request) {
        return WatchRequest.builder()
                .tenant(request.getTenant())
                .namespace(request.getNamespace())
                .appId(request.getAppId())
                .labels(LabelSet.of(request.getLabelsMap()))
                .lastKnownVersion(request.getLastVersion())
                .client(io.lighting.config.core.dto.ClientMetadata.builder()
                        .clientId(request.getMetaOrDefault("clientId", "unknown"))
                        .attributes(request.getMetaMap())
                        .build())
                .build();
    }

    public static ContentType contentTypeOrDefault(String value) {
        return ContentType.tryParse(value).orElse(ContentType.TEXT);
    }
}
