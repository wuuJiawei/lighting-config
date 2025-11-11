package io.lighting.config.embedded;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lighting.config.client.transport.PollingTransport;
import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.server.config.LightingServerProperties;
import io.lighting.config.server.config.ServerInfrastructureConfiguration;
import io.lighting.config.server.notify.NotifyEngine;
import io.lighting.config.server.service.ConfigApplicationService;
import io.lighting.config.spring.boot.autoconfigure.LightingClientAutoConfiguration;
import io.lighting.config.spring.boot.autoconfigure.LightingClientListenerConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnClass(io.lighting.config.server.rest.ConfigController.class)
@ConditionalOnProperty(prefix = "lighting.config", name = "mode", havingValue = "embedded")
@EnableConfigurationProperties({LightingEmbeddedProperties.class, LightingServerProperties.class})
@Import({ServerInfrastructureConfiguration.class,
        LightingClientAutoConfiguration.class,
        LightingClientListenerConfiguration.class})
@ComponentScan(basePackages = "io.lighting.config.server.rest")
public class LightingEmbeddedAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(ConfigRepository.class)
    public ConfigRepository embeddedConfigRepository(LightingEmbeddedProperties properties,
                                                     ObjectProvider<ObjectMapper> objectMapperProvider) {
        EmbeddedConfigOptions.StorageType type = properties.getStorage().getType();
        if (type == EmbeddedConfigOptions.StorageType.FILE) {
            ObjectMapper mapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
            return new FileSystemEmbeddedRepository(resolvePath(properties), mapper);
        }
        return new InMemoryEmbeddedRepository();
    }

    private java.nio.file.Path resolvePath(LightingEmbeddedProperties properties) {
        if (properties.getStorage().getPath() != null) {
            return properties.getStorage().getPath();
        }
        return java.nio.file.Paths.get(System.getProperty("user.home"), ".lighting-config.json");
    }

    @Bean
    @ConditionalOnMissingBean(PollingTransport.class)
    public PollingTransport embeddedConfigTransport(ConfigApplicationService applicationService,
                                                    NotifyEngine notifyEngine) {
        return new EmbeddedConfigTransport(applicationService, notifyEngine);
    }
}
