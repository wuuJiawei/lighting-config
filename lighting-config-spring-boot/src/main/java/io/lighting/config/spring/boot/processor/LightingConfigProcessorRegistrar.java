package io.lighting.config.spring.boot.processor;

import io.lighting.config.client.LightingClient;
import io.lighting.config.client.value.ValueDecoderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;

/**
 * Registers configuration-related bean post-processors after the context is fully refreshed
 * to avoid premature initialization issues.
 */
public class LightingConfigProcessorRegistrar implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(LightingConfigProcessorRegistrar.class);

    private final ConfigurableApplicationContext context;
    private final LightingClient client;
    private final ObjectProvider<ConversionService> conversionServiceProvider;
    private final ValueDecoderRegistry decoderRegistry;

    public LightingConfigProcessorRegistrar(ConfigurableApplicationContext context,
                                            LightingClient client,
                                            ObjectProvider<ConversionService> conversionServiceProvider,
                                            ValueDecoderRegistry decoderRegistry) {
        this.context = context;
        this.client = client;
        this.conversionServiceProvider = conversionServiceProvider;
        this.decoderRegistry = decoderRegistry;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        context.getBeanFactory().addBeanPostProcessor(
                new LightingValueBeanPostProcessor(client, conversionServiceProvider, decoderRegistry));
        context.getBeanFactory().addBeanPostProcessor(
                new LightingPropertiesBeanPostProcessor(client, conversionServiceProvider, decoderRegistry));
        log.info("Lighting configuration bean post-processors registered after context refresh.");
    }
}
