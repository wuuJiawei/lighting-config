package io.lighting.config.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.core.util.TimeProvider;
import io.lighting.config.server.repository.JdbcConfigRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(prefix = "lighting.config.storage", name = "type", havingValue = "jdbc", matchIfMissing = true)
public class DatabaseConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")
    public DataSource lightingDataSource(DataSourceProperties properties) {
        String url = properties.determineUrl();
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("spring.datasource.url must be set when lighting.config.storage.type=jdbc");
        }
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    public NamedParameterJdbcTemplate lightingJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnBean(NamedParameterJdbcTemplate.class)
    @ConditionalOnMissingBean(ConfigRepository.class)
    public ConfigRepository jdbcConfigRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                                 ObjectMapper objectMapper,
                                                 TimeProvider timeProvider) {
        return new JdbcConfigRepository(jdbcTemplate, objectMapper, timeProvider);
    }
}
