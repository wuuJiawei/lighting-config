package io.lighting.config.spring.boot.processor;

import io.lighting.config.client.LightingClient;
import io.lighting.config.client.cache.ConfigCache;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.client.value.ValueDecoderRegistry;
import io.lighting.config.core.model.ContentType;
import io.lighting.config.spring.boot.annotation.LightingProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LightingPropertiesBeanPostProcessor implements BeanPostProcessor {

    private final LightingClient client;
    private final ConversionService conversionService;
    private final ValueDecoderRegistry decoderRegistry;

    public LightingPropertiesBeanPostProcessor(LightingClient client,
                                               ObjectProvider<ConversionService> conversionServiceProvider,
                                               ValueDecoderRegistry decoderRegistry) {
        this.client = client;
        this.conversionService = conversionServiceProvider.getIfAvailable(DefaultConversionService::new);
        this.decoderRegistry = decoderRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        LightingProperties annotation = bean.getClass().getAnnotation(LightingProperties.class);
        if (annotation == null) {
            return bean;
        }
        String prefix = normalize(annotation.prefix());
        Map<String, FieldBinding> fieldMapping = bindFields(bean, prefix);
        if (annotation.autoRefresh() && !fieldMapping.isEmpty()) {
            client.addListener(prefix, change -> refreshField(bean, fieldMapping, change));
        }
        return bean;
    }

    private Map<String, FieldBinding> bindFields(Object bean, String prefix) {
        Map<String, FieldBinding> mapping = new HashMap<>();
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                return;
            }
            ReflectionUtils.makeAccessible(field);
            Object currentValue = ReflectionUtils.getField(field, bean);
            FieldBinding binding = new FieldBinding(field, field.getGenericType());
            List<String> candidateKeys = KeyVariantUtils.fieldVariants(prefix, field.getName());
            Object resolved = resolveValue(candidateKeys, binding.targetType()).orElse(currentValue);
            if (resolved != null) {
                ReflectionUtils.setField(binding.field(), bean, resolved);
            }
            candidateKeys.forEach(key -> mapping.put(key, binding));
        });
        return mapping;
    }

    private void refreshField(Object bean, Map<String, FieldBinding> mapping, ConfigChange change) {
        FieldBinding binding = mapping.get(change.getCoordinate().getKey());
        if (binding == null) {
            return;
        }
        Object converted = convert(change.getValue(), change.getContentType(), binding.targetType());
        ReflectionUtils.setField(binding.field(), bean, converted);
    }

    private Optional<Object> resolveValue(List<String> keys, Type targetType) {
        for (String key : keys) {
            Optional<Object> value = resolveValue(key, targetType);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    private Optional<Object> resolveValue(String key, Type targetType) {
        Optional<ConfigCache.Snapshot> snapshot = client.getSnapshot(key);
        if (snapshot.isEmpty()) {
            return Optional.empty();
        }
        ConfigCache.Snapshot cached = snapshot.get();
        ContentType contentType = ContentType.fromAlias(cached.contentType());
        return Optional.ofNullable(convert(cached.value(), contentType, targetType));
    }

    private Object convert(String value, ContentType contentType, Type targetType) {
        return ValueConversionSupport.convert(value,
                contentType == null ? ContentType.STRING : contentType,
                targetType,
                decoderRegistry,
                conversionService,
                "@LightingProperties field type=" + targetType.getTypeName() + ", contentType=" + contentType.name());
    }

    private String normalize(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return "";
        }
        return prefix.endsWith(".") ? prefix : prefix + ".";
    }

    private static final class FieldBinding {
        private final Field field;
        private final Type targetType;

        private FieldBinding(Field field, Type targetType) {
            this.field = field;
            this.targetType = targetType;
        }

        public Field field() {
            return field;
        }

        public Type targetType() {
            return targetType;
        }
    }
}
