package io.lighting.config.spring.boot.annotation;

import io.lighting.config.spring.boot.autoconfigure.LightingClientAutoConfiguration;
import io.lighting.config.spring.boot.autoconfigure.LightingClientListenerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({LightingClientAutoConfiguration.class, LightingClientListenerConfiguration.class})
public @interface EnableLightingConfig {
}
