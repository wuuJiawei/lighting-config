package io.lighting.config.spring.boot.autoconfigure;

import io.lighting.config.client.LightingClient;
import io.lighting.config.client.config.ClientOptions;
import io.lighting.config.client.transport.HttpPollingTransport;
import io.lighting.config.client.transport.PollingTransport;
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
                .pollInterval(properties.getPollInterval())
                .bannerEnabled(properties.isBannerEnabled())
                .build();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(PollingTransport.class)
    public PollingTransport lightingConfigTransport(ClientOptions options) {
        return new HttpPollingTransport(options);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public LightingClient lightingClient(ClientOptions options,
                                         PollingTransport transport,
                                         LightingClientProperties properties) {
        LightingClient client = new LightingClient(options, transport);
        if (properties.isAutoStart()) {
            client.start();
        }
        return client;
    }
}
