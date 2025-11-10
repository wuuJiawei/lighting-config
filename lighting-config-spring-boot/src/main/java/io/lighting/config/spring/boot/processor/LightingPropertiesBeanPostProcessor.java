package io.lighting.config.spring.boot.processor;

import io.lighting.config.client.LightingClient;
import io.lighting.config.client.cache.ConfigCache;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.spring.boot.annotation.LightingProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LightingPropertiesBeanPostProcessor implements BeanPostProcessor {

    private final LightingClient client;
    private final ConversionService conversionService;

    public LightingPropertiesBeanPostProcessor(LightingClient client,
                                               ObjectProvider<ConversionService> conversionServiceProvider) {
        this.client = client;
        this.conversionService = conversionServiceProvider.getIfAvailable(DefaultConversionService::new);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        LightingProperties annotation = bean.getClass().getAnnotation(LightingProperties.class);
        if (annotation == null) {
            return bean;
        }
        String prefix = normalize(annotation.prefix());
        Map<String, Field> fieldMapping = bindFields(bean, prefix);
        if (annotation.autoRefresh() && !fieldMapping.isEmpty()) {
            client.addListener(prefix, change -> refreshField(bean, fieldMapping, change));
        }
        return bean;
    }

    private Map<String, Field> bindFields(Object bean, String prefix) {
        Map<String, Field> mapping = new HashMap<>();
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                return;
            }
            String key = prefix + fieldToKey(field.getName());
            ReflectionUtils.makeAccessible(field);
            Object currentValue = ReflectionUtils.getField(field, bean);
            Object resolved = resolveValue(key, field.getType()).orElse(currentValue);
            if (resolved != null) {
                ReflectionUtils.setField(field, bean, resolved);
            }
            mapping.put(key, field);
        });
        return mapping;
    }

    private void refreshField(Object bean, Map<String, Field> mapping, ConfigChange change) {
        Field field = mapping.get(change.getCoordinate().getKey());
        if (field == null) {
            return;
        }
        Object converted = convert(change.getValue(), field.getType());
        ReflectionUtils.setField(field, bean, converted);
    }

    private Optional<Object> resolveValue(String key, Class<?> targetType) {
        Optional<ConfigCache.Snapshot> snapshot = client.getSnapshot(key);
        if (snapshot.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(convert(snapshot.get().value(), targetType));
    }

    private Object convert(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        }
        if (value == null) {
            return null;
        }
        if (conversionService.canConvert(String.class, targetType)) {
            return conversionService.convert(value, targetType);
        }
        throw new IllegalStateException("Unsupported type for @LightingProperties binding: " + targetType.getName());
    }

    private String normalize(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return "";
        }
        return prefix.endsWith(".") ? prefix : prefix + ".";
    }

    private String fieldToKey(String name) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                builder.append('.').append(Character.toLowerCase(ch));
            } else {
                builder.append(ch);
            }
        }
        String result = builder.toString();
        if (result.startsWith(".")) {
            result = result.substring(1);
        }
        return result;
    }
}
