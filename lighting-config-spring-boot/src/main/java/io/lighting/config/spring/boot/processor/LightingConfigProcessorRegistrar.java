package io.lighting.config.spring.boot.processor;

import io.lighting.config.client.LightingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;

public class LightingConfigProcessorRegistrar implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(LightingConfigProcessorRegistrar.class);
    private final ConfigurableApplicationContext context;
    private final LightingClient client;
    private final ObjectProvider<ConversionService> conversionService;

    public LightingConfigProcessorRegistrar(ConfigurableApplicationContext context,
                                            LightingClient client,
                                            ObjectProvider<ConversionService> conversionService) {
        this.context = context;
        this.client = client;
        this.conversionService = conversionService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        context.getBeanFactory().addBeanPostProcessor(
                new LightingValueBeanPostProcessor(client, conversionService)
        );
        context.getBeanFactory().addBeanPostProcessor(
                new LightingPropertiesBeanPostProcessor(client, conversionService)
        );
        log.info("LightingConfigBeanPostProcessor registered after context refresh.");
    }
}
