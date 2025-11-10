package io.lighting.config.client.transport;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.lighting.config.client.config.ClientOptions;
import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.dto.WatchRequest;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ContentType;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.link.proto.ConfigServiceGrpc;
import io.lighting.config.link.proto.ConfigUpdate;
import io.lighting.config.link.proto.PullRequest;
import io.lighting.config.link.proto.PullResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class GrpcConfigTransport implements ConfigTransport {

    private static final Logger log = LoggerFactory.getLogger(GrpcConfigTransport.class);

    private final ClientOptions options;
    private final ManagedChannel channel;
    private final ConfigServiceGrpc.ConfigServiceBlockingStub blockingStub;
    private final ConfigServiceGrpc.ConfigServiceStub asyncStub;

    public GrpcConfigTransport(ClientOptions options) {
        this.options = options;
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget(options.getServerAddress());
        if (!options.isUseTls()) {
            builder.usePlaintext();
        }
        this.channel = builder.build();
        this.blockingStub = ConfigServiceGrpc.newBlockingStub(channel);
        this.asyncStub = ConfigServiceGrpc.newStub(channel);
    }

    @Override
    public List<ConfigItem> pull(PullQuery query) {
        PullRequest.Builder builder = PullRequest.newBuilder()
                .setTenant(query.getTenant())
                .setNamespace(query.getNamespace())
                .setAppId(query.getAppId());
        query.getSelector().getKey().ifPresent(builder::setKey);
        query.getSelector().getPrefix().ifPresent(builder::setPrefix);
        PullResponse response = blockingStub.pull(builder.build());
        List<ConfigItem> result = new ArrayList<>();
        for (ConfigUpdate update : response.getItemsList()) {
            result.add(toConfigItem(update));
        }
        return result;
    }

    @Override
    public WatchHandle watch(WatchRequest request, Consumer<ConfigChange> consumer) {
        io.lighting.config.link.proto.WatchRequest proto = io.lighting.config.link.proto.WatchRequest.newBuilder()
                .setTenant(request.getTenant())
                .setNamespace(request.getNamespace())
                .setAppId(request.getAppId())
                .putAllLabels(request.getLabels().asMap())
                .putAllMeta(request.getClient().getAttributes())
                .setLastVersion(request.getLastKnownVersion())
                .build();
        AtomicBoolean closed = new AtomicBoolean(false);
        StreamObserver<ConfigUpdate> observer = new StreamObserver<>() {
            @Override
            public void onNext(ConfigUpdate value) {
                consumer.accept(toConfigChange(value));
            }

            @Override
            public void onError(Throwable t) {
                if (!closed.get()) {
                    log.warn("Watch stream error: {}", t.getMessage());
                }
            }

            @Override
            public void onCompleted() {
                log.info("Watch stream completed");
            }
        };
        asyncStub.watch(proto, observer);
        return () -> {
            closed.set(true);
        };
    }

    private ConfigItem toConfigItem(ConfigUpdate update) {
        return ConfigItem.builder()
                .tenant(options.getTenant())
                .namespace(options.getNamespace())
                .appId(options.getAppId())
                .key(update.getKey())
                .value(update.getValue())
                .contentType(ContentType.fromAlias(update.getContentType()))
                .version(update.getVersion())
                .enabled(!update.getDeleted())
                .createdAt(Instant.ofEpochMilli(update.getOccurredAt()))
                .updatedAt(Instant.ofEpochMilli(update.getOccurredAt()))
                .build();
    }

    private ConfigChange toConfigChange(ConfigUpdate update) {
        return ConfigChange.builder()
                .coordinate(ConfigCoordinate.of(options.getTenant(), options.getNamespace(), options.getAppId(), update.getKey()))
                .version(update.getVersion())
                .type(update.getDeleted() ? ChangeType.DELETE : ChangeType.UPSERT)
                .contentType(ContentType.fromAlias(update.getContentType()))
                .value(update.getValue())
                .deleted(update.getDeleted())
                .occurredAt(Instant.ofEpochMilli(update.getOccurredAt()))
                .build();
    }

    @Override
    public void close() {
        channel.shutdown();
    }
}
