package io.lighting.config.embedded;

import io.lighting.config.embedded.web.EmbeddedRestMvcConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Dedicated DispatcherServlet that lazily创建子 WebApplicationContext，
 * 确保只在 ServletContext 可用时刷新 REST 层。
 */
class LightingEmbeddedDispatcherServlet extends DispatcherServlet {

    private final ApplicationContext parentContext;

    LightingEmbeddedDispatcherServlet(ApplicationContext parentContext) {
        this.parentContext = parentContext;
    }

    @Override
    protected WebApplicationContext createWebApplicationContext(@Nullable WebApplicationContext parent) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        ApplicationContext resolvedParent = parent != null ? parent : parentContext;
        if (resolvedParent != null) {
            context.setParent(resolvedParent);
        }
        context.register(EmbeddedRestMvcConfiguration.class);
        return context;
    }
}
