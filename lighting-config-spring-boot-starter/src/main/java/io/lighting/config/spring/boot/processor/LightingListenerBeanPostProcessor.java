package io.lighting.config.spring.boot.processor;

import io.lighting.config.client.LightingClient;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.spring.boot.annotation.LightingListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class LightingListenerBeanPostProcessor implements BeanPostProcessor {

    private final LightingClient client;

    public LightingListenerBeanPostProcessor(LightingClient client) {
        this.client = client;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            LightingListener annotation = method.getAnnotation(LightingListener.class);
            if (annotation != null) {
                registerListener(bean, method, annotation);
            }
        });
        return bean;
    }

    private void registerListener(Object bean, Method method, LightingListener annotation) {
        ReflectionUtils.makeAccessible(method);
        client.addListener(annotation.prefix(), change -> invoke(bean, method, annotation, change));
    }

    private void invoke(Object bean, Method method, LightingListener annotation, ConfigChange change) {
        if (annotation.prefix() != null && !annotation.prefix().isEmpty()) {
            if (!change.getCoordinate().getKey().startsWith(annotation.prefix())) {
                return;
            }
        }
        Class<?>[] params = method.getParameterTypes();
        try {
            if (params.length == 0) {
                method.invoke(bean);
            } else if (params.length == 1 && params[0].isAssignableFrom(ConfigChange.class)) {
                method.invoke(bean, change);
            } else {
                throw new IllegalStateException("@LightingListener method must have 0 or 1 ConfigChange parameter");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke @LightingListener method", e);
        }
    }
}
