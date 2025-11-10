package io.lighting.config.server.grpc;

import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.dto.WatchRequest;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.link.proto.ConfigServiceGrpc;
import io.lighting.config.link.proto.ConfigUpdate;
import io.lighting.config.link.proto.HeartbeatRequest;
import io.lighting.config.link.proto.HeartbeatResponse;
import io.lighting.config.link.proto.PullRequest;
import io.lighting.config.link.proto.PullResponse;
import io.lighting.config.server.notify.NotifyEngine;
import io.lighting.config.server.service.ConfigApplicationService;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GrpcConfigService extends ConfigServiceGrpc.ConfigServiceImplBase {

    private final ConfigApplicationService applicationService;
    private final NotifyEngine notifyEngine;

    public GrpcConfigService(ConfigApplicationService applicationService, NotifyEngine notifyEngine) {
        this.applicationService = applicationService;
        this.notifyEngine = notifyEngine;
    }

    @Override
    public void watch(io.lighting.config.link.proto.WatchRequest request,
                      StreamObserver<ConfigUpdate> responseObserver) {
        WatchRequest domainRequest = GrpcModelMapper.toWatchRequest(request);
        ServerCallStreamObserver<ConfigUpdate> serverObserver = (ServerCallStreamObserver<ConfigUpdate>) responseObserver;
        WatchSubscriber subscriber = new WatchSubscriber(domainRequest, serverObserver);
        NotifyEngine.Registration registration = notifyEngine.register(subscriber);
        serverObserver.setOnCancelHandler(() -> {
            subscriber.close();
            registration.close();
        });
        serverObserver.setOnReadyHandler(() -> sendSnapshot(domainRequest, serverObserver));
    }

    @Override
    public void pull(PullRequest request, StreamObserver<PullResponse> responseObserver) {
        try {
            PullQuery query = GrpcModelMapper.toPullQuery(request);
            List<ConfigItem> items = applicationService.list(query);
            PullResponse.Builder builder = PullResponse.newBuilder();
            items.stream().map(GrpcModelMapper::toProto).forEach(builder::addItems);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        HeartbeatResponse response = HeartbeatResponse.newBuilder()
                .setStatus("OK")
                .setServerTime(Instant.now().toEpochMilli())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void sendSnapshot(WatchRequest request, StreamObserver<ConfigUpdate> observer) {
        PullQuery query = PullQuery.builder()
                .tenant(request.getTenant())
                .namespace(request.getNamespace())
                .appId(request.getAppId())
                .build();
        applicationService.list(query).stream()
                .map(GrpcModelMapper::toProto)
                .forEach(observer::onNext);
    }

    private final class WatchSubscriber implements NotifyEngine.Subscriber {
        private final WatchRequest request;
        private final ServerCallStreamObserver<ConfigUpdate> observer;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        private WatchSubscriber(WatchRequest request, ServerCallStreamObserver<ConfigUpdate> observer) {
            this.request = request;
            this.observer = observer;
        }

        @Override
        public boolean matches(io.lighting.config.core.dto.ConfigChangeEvent event) {
            return request.getTenant().equals(event.getCoordinate().getTenant())
                    && request.getNamespace().equals(event.getCoordinate().getNamespace())
                    && request.getAppId().equals(event.getCoordinate().getAppId());
        }

        @Override
        public void onEvent(io.lighting.config.core.dto.ConfigChangeEvent event) {
            if (closed.get() || !observer.isReady()) {
                return;
            }
            observer.onNext(GrpcModelMapper.toProto(event));
        }

        private void close() {
            closed.set(true);
        }
    }
}
