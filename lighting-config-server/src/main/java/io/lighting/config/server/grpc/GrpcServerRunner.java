package io.lighting.config.server.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.lighting.config.server.config.LightingServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.util.List;

public class GrpcServerRunner implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerRunner.class);

    private final LightingServerProperties properties;
    private final List<BindableService> services;

    private Server server;
    private volatile boolean running;

    public GrpcServerRunner(LightingServerProperties properties, List<BindableService> services) {
        this.properties = properties;
        this.services = services;
    }

    @Override
    public void start() {
        if (!properties.getGrpc().isEnabled() || running) {
            return;
        }
        try {
            NettyServerBuilder builder = NettyServerBuilder.forPort(properties.getGrpc().getPort());
            services.forEach(builder::addService);
            server = builder.build().start();
            running = true;
            log.info("gRPC server started on port {}", properties.getGrpc().getPort());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start gRPC server", e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
            running = false;
            log.info("gRPC server stopped");
        }
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
