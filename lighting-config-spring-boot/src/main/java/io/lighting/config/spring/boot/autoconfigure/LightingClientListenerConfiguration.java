package io.lighting.config.spring.boot.autoconfigure;

import io.lighting.config.client.LightingClient;
import io.lighting.config.spring.boot.processor.LightingListenerBeanPostProcessor;
import io.lighting.config.spring.boot.processor.LightingConfigProcessorRegistrar;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

@Configuration
@ConditionalOnBean(LightingClient.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class LightingClientListenerConfiguration {

    @Bean
    public LightingConfigProcessorRegistrar lightingValueProcessorRegistrar(
            ConfigurableApplicationContext context,
            LightingClient client,
            ObjectProvider<ConversionService> conversionService) {
        return new LightingConfigProcessorRegistrar(context, client, conversionService);
    }

    @Bean
    public LightingListenerBeanPostProcessor lightingListenerBeanPostProcessor(LightingClient client) {
        return new LightingListenerBeanPostProcessor(client);
    }

}
