package io.lighting.config.example.server;

import io.lighting.config.core.util.TimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("server")
public class ExampleServerConfiguration {

    @Bean
    public TimeProvider timeProvider() {
        return TimeProvider.system();
    }

    @Bean
    public ConfigApplicationService configApplicationService(TimeProvider timeProvider) {
        return new InMemoryConfigApplicationService(timeProvider);
    }
}
