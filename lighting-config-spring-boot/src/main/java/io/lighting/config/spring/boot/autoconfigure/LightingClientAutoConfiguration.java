package io.lighting.config.spring.boot.autoconfigure;

import io.lighting.config.client.LightingClient;
import io.lighting.config.client.config.ClientOptions;
import io.lighting.config.client.transport.ConfigTransport;
import io.lighting.config.client.transport.GrpcConfigTransport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(LightingClient.class)
@EnableConfigurationProperties(LightingClientProperties.class)
@ConditionalOnProperty(prefix = "lighting.config.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LightingClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ClientOptions lightingClientOptions(LightingClientProperties properties) {
        return ClientOptions.builder()
                .serverAddress(properties.getServer().getAddress())
                .useTls(properties.getServer().isTls())
                .tenant(properties.getTenant())
                .namespace(properties.getNamespace())
                .appId(properties.getAppId())
                .labels(properties.getLabels())
                .metadata(properties.getMetadata())
                .bootstrapPrefixes(properties.getBootstrapPrefixes())
                .watchReconnectBackoff(properties.getWatchReconnectBackoff())
                .bannerEnabled(properties.isBannerEnabled())
                .build();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(ConfigTransport.class)
    public ConfigTransport lightingConfigTransport(LightingClientProperties properties,
                                                   ClientOptions options) {
        if (!isGrpcAddress(options.getServerAddress())) {
            throw new IllegalStateException("lighting.config.client.server.address must start with dns:// or direct:// when using gRPC transport");
        }
        return new GrpcConfigTransport(options);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public LightingClient lightingClient(ClientOptions options,
                                         ConfigTransport transport,
                                         LightingClientProperties properties) {
        LightingClient client = new LightingClient(options, transport);
        if (properties.isAutoStart()) {
            client.start();
        }
        return client;
    }
    private boolean isGrpcAddress(String address) {
        if (address == null) {
            return false;
        }
        return address.startsWith("dns://") || address.startsWith("direct://");
    }
}
