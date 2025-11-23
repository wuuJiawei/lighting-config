package io.lighting.config.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lighting.config.core.api.ConfigRepository;
import io.lighting.config.core.util.TimeProvider;
import io.lighting.config.server.monitoring.CacheMissAlertRepository;
import io.lighting.config.server.monitoring.JdbcCacheMissAlertRepository;
import io.lighting.config.server.repository.JdbcConfigRepository;
import io.lighting.config.server.repository.JdbcRevisionRepository;
import io.lighting.config.server.repository.RevisionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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

    @Bean(name = "rawConfigRepository")
    @ConditionalOnBean(NamedParameterJdbcTemplate.class)
    @ConditionalOnMissingBean(name = "rawConfigRepository")
    public ConfigRepository jdbcConfigRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                                 ObjectMapper objectMapper,
                                                 TimeProvider timeProvider) {
        return new JdbcConfigRepository(jdbcTemplate, objectMapper, timeProvider);
    }

    @Bean
    @ConditionalOnBean(NamedParameterJdbcTemplate.class)
    @Primary
    public CacheMissAlertRepository cacheMissAlertJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        return new JdbcCacheMissAlertRepository(jdbcTemplate);
    }

    @Bean
    @ConditionalOnBean(NamedParameterJdbcTemplate.class)
    public RevisionRepository revisionJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        return new JdbcRevisionRepository(jdbcTemplate);
    }
}
