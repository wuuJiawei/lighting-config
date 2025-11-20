package io.lighting.config.spring.boot.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LightingProperties {

    /**
     * Prefix used to resolve keys from lighting-config (e.g. "feature.").
     */
    String prefix();

    /**
     * Whether updates from the server should automatically refresh the bean fields.
     */
    boolean autoRefresh() default true;
}
