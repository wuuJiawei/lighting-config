package io.lighting.config.server.config;

import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.core.api.EventBus;
import io.lighting.config.core.util.TimeProvider;
import io.lighting.config.server.notify.InMemoryNotifyEngine;
import io.lighting.config.server.notify.NotifyEngine;
import io.lighting.config.server.notify.SimpleEventBus;
import io.lighting.config.server.service.ConfigApplicationService;
import io.lighting.config.server.service.DefaultConfigApplicationService;
import io.lighting.config.server.repository.InMemoryConfigRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerInfrastructureConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventBus eventBus() {
        return new SimpleEventBus();
    }

    @Bean
    @ConditionalOnMissingBean
    public NotifyEngine notifyEngine() {
        return new InMemoryNotifyEngine();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "lighting.storage", name = "type", havingValue = "memory")
    public ConfigRepository configRepository() {
        return new InMemoryConfigRepository();
    }

    @Bean
    public ConfigApplicationService configApplicationService(ConfigRepository repository,
                                                             NotifyEngine notifyEngine,
                                                             TimeProvider timeProvider) {
        return new DefaultConfigApplicationService(repository, notifyEngine, timeProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public TimeProvider timeProvider() {
        return TimeProvider.system();
    }
}
