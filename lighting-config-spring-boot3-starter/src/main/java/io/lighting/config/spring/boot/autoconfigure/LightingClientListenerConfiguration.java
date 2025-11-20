package io.lighting.config.spring.boot.autoconfigure;

import io.lighting.config.client.LightingClient;
import io.lighting.config.client.value.ConfigValueDecoder;
import io.lighting.config.client.value.ValueDecoderRegistry;
import io.lighting.config.spring.boot.processor.LightingConfigProcessorRegistrar;
import io.lighting.config.spring.boot.processor.LightingListenerBeanPostProcessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

import java.util.stream.Collectors;

@Configuration
@ConditionalOnBean(LightingClient.class)
public class LightingClientListenerConfiguration {

    @Bean
    public LightingConfigProcessorRegistrar lightingValueProcessorRegistrar(
            ConfigurableApplicationContext context,
            LightingClient client,
            ObjectProvider<ConversionService> conversionService,
            ValueDecoderRegistry decoderRegistry) {
        return new LightingConfigProcessorRegistrar(context, client, conversionService, decoderRegistry);
    }

    @Bean
    public LightingListenerBeanPostProcessor lightingListenerBeanPostProcessor(LightingClient client) {
        return new LightingListenerBeanPostProcessor(client);
    }

    @Bean
    public ValueDecoderRegistry lightingValueDecoderRegistry(ObjectProvider<ConfigValueDecoder> decoderProvider) {
        return ValueDecoderRegistry.withDefaults(
                decoderProvider.orderedStream().collect(Collectors.toList()));
    }
}
