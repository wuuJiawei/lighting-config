package io.lighting.config.server.config;

import io.grpc.BindableService;
import io.lighting.config.server.grpc.GrpcConfigService;
import io.lighting.config.server.grpc.GrpcServerRunner;
import io.lighting.config.server.notify.NotifyEngine;
import io.lighting.config.server.service.ConfigApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "lighting.config.grpc", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GrpcServerConfiguration {

    @Bean
    public GrpcConfigService grpcConfigService(ConfigApplicationService applicationService,
                                               NotifyEngine notifyEngine) {
        return new GrpcConfigService(applicationService, notifyEngine);
    }

    @Bean
    public GrpcServerRunner grpcServerRunner(LightingServerProperties properties,
                                             List<BindableService> services) {
        return new GrpcServerRunner(properties, services);
    }
}
