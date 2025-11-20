package io.lighting.config.spring.boot.processor;

import io.lighting.config.client.LightingClient;
import io.lighting.config.client.cache.ConfigCache;
import io.lighting.config.client.value.ValueDecoderRegistry;
import io.lighting.config.core.model.ContentType;
import io.lighting.config.spring.boot.annotation.LightingValue;
import io.lighting.config.core.dto.ConfigChange;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class LightingValueBeanPostProcessor implements BeanPostProcessor {

    private final LightingClient client;
    private final ConversionService conversionService;
    private final ValueDecoderRegistry decoderRegistry;

    public LightingValueBeanPostProcessor(LightingClient client,
                                          ObjectProvider<ConversionService> conversionServiceProvider,
                                          ValueDecoderRegistry decoderRegistry) {
        this.client = client;
        this.conversionService = conversionServiceProvider.getIfAvailable(DefaultConversionService::new);
        this.decoderRegistry = decoderRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 跳过 Spring 自身的 Web/Framework Bean
        String packageName = bean.getClass().getPackageName();
        if (packageName.startsWith("org.springframework.")) {
            return bean;
        }

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
        Type targetType = field.getGenericType();
        List<String> keyVariants = KeyVariantUtils.explicitKeyVariants(annotation.key());
        Object initialValue = resolveValue(keyVariants, annotation.defaultValue(), targetType);
        ReflectionUtils.setField(field, bean, initialValue);
        for (String key : keyVariants) {
            client.addListener(key, change -> {
                if (!key.equals(change.getCoordinate().getKey())) {
                    return;
                }
                Object value = convert(change.getValue(), change.getContentType(), targetType);
                ReflectionUtils.setField(field, bean, value);
            });
        }
    }

    private Object resolveValue(List<String> keys, String defaultValue, Type targetType) {
        for (String key : keys) {
            Optional<ConfigCache.Snapshot> snapshot = client.getSnapshot(key);
            if (snapshot.isPresent()) {
                ConfigCache.Snapshot cached = snapshot.get();
                ContentType contentType = ContentType.fromAlias(cached.contentType());
                return convert(cached.value(), contentType, targetType);
            }
        }
        return convert(defaultValue, ContentType.STRING, targetType);
    }

    private Object convert(String value, ContentType contentType, Type targetType) {
        return ValueConversionSupport.convert(value,
                contentType == null ? ContentType.STRING : contentType,
                targetType,
                decoderRegistry,
                conversionService,
                fieldDescription(targetType, "@LightingValue", contentType));
    }

    private String fieldDescription(Type targetType, String annotation, ContentType contentType) {
        return annotation + " target type=" + targetType.getTypeName() + ", contentType=" + contentType.name();
    }
}
