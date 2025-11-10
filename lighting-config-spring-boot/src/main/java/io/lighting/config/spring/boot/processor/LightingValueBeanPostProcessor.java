package io.lighting.config.spring.boot.processor;

import io.lighting.config.client.LightingClient;
import io.lighting.config.client.cache.ConfigCache;
import io.lighting.config.spring.boot.annotation.LightingValue;
import io.lighting.config.core.dto.ConfigChange;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Optional;

public class LightingValueBeanPostProcessor implements BeanPostProcessor {

    private final LightingClient client;
    private final ConversionService conversionService;

    public LightingValueBeanPostProcessor(LightingClient client,
                                          ObjectProvider<ConversionService> conversionServiceProvider) {
        this.client = client;
        this.conversionService = conversionServiceProvider.getIfAvailable(DefaultConversionService::new);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            LightingValue annotation = field.getAnnotation(LightingValue.class);
            if (annotation != null) {
                registerField(bean, field, annotation);
            }
        });
        return bean;
    }

    private void registerField(Object bean, Field field, LightingValue annotation) {
        ReflectionUtils.makeAccessible(field);
        Object initialValue = resolveValue(annotation, field.getType());
        ReflectionUtils.setField(field, bean, initialValue);
        client.addListener(annotation.key(), change -> {
            if (!annotation.key().equals(change.getCoordinate().getKey())) {
                return;
            }
            Object value = convert(change.getValue(), field.getType());
            ReflectionUtils.setField(field, bean, value);
        });
    }

    private Object resolveValue(LightingValue annotation, Class<?> targetType) {
        Optional<ConfigCache.Snapshot> snapshot = client.getSnapshot(annotation.key());
        String raw = snapshot.map(ConfigCache.Snapshot::value).orElse(annotation.defaultValue());
        return convert(raw, targetType);
    }

    private Object convert(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        }
        if (conversionService.canConvert(String.class, targetType)) {
            return conversionService.convert(value, targetType);
        }
        throw new IllegalStateException("Unsupported field type for @LightingValue: " + targetType.getName());
    }
}
