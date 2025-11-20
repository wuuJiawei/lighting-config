package io.lighting.config.spring.boot.processor;

import io.lighting.config.client.LightingClient;
import io.lighting.config.client.value.ValueDecoderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;

/**
 * 在应用启动完成后再注册自定义 BeanPostProcessor，并对已存在的 Bean 进行一次补偿处理，避免默认值未被覆盖。
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
        LightingValueBeanPostProcessor valueBpp =
                new LightingValueBeanPostProcessor(client, conversionServiceProvider, decoderRegistry);
        LightingPropertiesBeanPostProcessor propsBpp =
                new LightingPropertiesBeanPostProcessor(client, conversionServiceProvider, decoderRegistry);

        context.getBeanFactory().addBeanPostProcessor(valueBpp);
        context.getBeanFactory().addBeanPostProcessor(propsBpp);
        log.info("Lighting configuration BeanPostProcessor registered after context refresh.");
        reprocessSingletons(valueBpp, propsBpp);
    }

    private void reprocessSingletons(BeanPostProcessor... processors) {
        String[] beanNames = context.getBeanFactory().getSingletonNames();
        for (String beanName : beanNames) {
            Object bean;
            try {
                bean = context.getBean(beanName);
            } catch (Exception ex) {
                log.debug("Skip reprocessing bean {} due to initialization error: {}", beanName, ex.getMessage());
                continue;
            }
            for (BeanPostProcessor processor : processors) {
                try {
                    processor.postProcessAfterInitialization(bean, beanName);
                } catch (Exception ex) {
                    log.warn("Failed to reprocess bean {} with {}: {}", beanName,
                            processor.getClass().getSimpleName(), ex.getMessage());
                }
            }
        }
    }
}
