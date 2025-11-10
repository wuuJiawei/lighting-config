package io.lighting.config.spring.boot.autoconfigure;

import io.lighting.config.client.LightingClient;
import io.lighting.config.spring.boot.processor.LightingListenerBeanPostProcessor;
import io.lighting.config.spring.boot.processor.LightingPropertiesBeanPostProcessor;
import io.lighting.config.spring.boot.processor.LightingValueBeanPostProcessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

@Configuration
public class LightingClientListenerConfiguration {

    @Bean
    public LightingValueBeanPostProcessor lightingValueBeanPostProcessor(LightingClient client,
                                                                         ObjectProvider<ConversionService> conversionService) {
        return new LightingValueBeanPostProcessor(client, conversionService);
    }

    @Bean
    public LightingListenerBeanPostProcessor lightingListenerBeanPostProcessor(LightingClient client) {
        return new LightingListenerBeanPostProcessor(client);
    }

    @Bean
    public LightingPropertiesBeanPostProcessor lightingPropertiesBeanPostProcessor(LightingClient client,
                                                                                  ObjectProvider<ConversionService> conversionService) {
        return new LightingPropertiesBeanPostProcessor(client, conversionService);
    }
}
