package io.lighting.config.spring.boot.autoconfigure;

import io.lighting.config.client.LightingClient;
import io.lighting.config.client.config.ClientOptions;
import io.lighting.config.client.transport.HttpPollingTransport;
import io.lighting.config.client.transport.PollingTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ConditionalOnClass(LightingClient.class)
@EnableConfigurationProperties(LightingClientProperties.class)
@ConditionalOnProperty(prefix = "lighting.config.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LightingClientAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LightingClientAutoConfiguration.class);

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
                .authToken(properties.getAuthToken())
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
                                         PollingTransport transport) {
        return new LightingClient(options, transport);
    }

    @Bean
    @ConditionalOnBean(LightingClient.class)
    public ApplicationListener<ApplicationReadyEvent> lightingClientStartupListener(LightingClient client,
                                                                                    LightingClientProperties properties) {
        return event -> {
            log.info("Lighting client startup event: {}", event);
            if (properties.isAutoStart()) {
                client.start();
            }
        };
    }
}
