package io.lighting.config.server.config;

import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.core.api.EventBus;
import io.lighting.config.core.util.TimeProvider;
import io.lighting.config.server.cache.CacheMissTracker;
import io.lighting.config.server.cache.ClientSnapshotService;
import io.lighting.config.server.config.LightingServerProperties;
import io.lighting.config.server.monitoring.CacheMissAlertRepository;
import io.lighting.config.server.monitoring.InMemoryCacheMissAlertRepository;
import io.lighting.config.server.notify.ChangeFeed;
import io.lighting.config.server.notify.InMemoryChangeFeed;
import io.lighting.config.server.notify.InMemoryNotifyEngine;
import io.lighting.config.server.notify.NotifyEngine;
import io.lighting.config.server.notify.SimpleEventBus;
import io.lighting.config.server.repository.InMemoryConfigRepository;
import io.lighting.config.server.service.ConfigApplicationService;
import io.lighting.config.server.service.DefaultConfigApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public ChangeFeed changeFeed() {
        return new InMemoryChangeFeed();
    }

    @Bean
    @ConditionalOnBean(name = "rawConfigRepository")
    @Primary
    public ConfigRepository configRepository(@Qualifier("rawConfigRepository") ConfigRepository rawRepository) {
        return rawRepository;
    }

    @Bean(name = "rawConfigRepository")
    @ConditionalOnMissingBean(name = "rawConfigRepository")
    @ConditionalOnProperty(prefix = "lighting.config.storage", name = "type", havingValue = "memory")
    public ConfigRepository inMemoryConfigRepository() {
        return new InMemoryConfigRepository();
    }

    @Bean
    public ConfigApplicationService configApplicationService(ConfigRepository repository,
                                                             NotifyEngine notifyEngine,
                                                             ChangeFeed changeFeed,
                                                             TimeProvider timeProvider,
                                                             ClientSnapshotService clientSnapshotService) {
        return new DefaultConfigApplicationService(repository, notifyEngine, changeFeed, timeProvider, clientSnapshotService);
    }

    @Bean
    @ConditionalOnMissingBean
    public TimeProvider timeProvider() {
        return TimeProvider.system();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheMissAlertRepository cacheMissAlertRepository() {
        return new InMemoryCacheMissAlertRepository();
    }

    @Bean
    public CacheMissTracker cacheMissTracker(CacheMissAlertRepository repository,
                                             TimeProvider timeProvider,
                                             LightingServerProperties properties) {
        int threshold = Math.max(1, properties.getMonitoring().getCacheMissThreshold());
        return new CacheMissTracker(repository, threshold, timeProvider);
    }

    @Bean
    public ClientSnapshotService clientSnapshotService(ConfigRepository repository,
                                                       CacheMissTracker missTracker) {
        return new ClientSnapshotService(repository, missTracker);
    }
}
