package io.lighting.config.server.config;

import io.lighting.config.server.service.AuthService;
import io.lighting.config.server.web.AuthTokenFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class WebComponentConfiguration {

    @Bean
    public FilterRegistrationBean<AuthTokenFilter> authTokenFilter(AuthService authService) {
        FilterRegistrationBean<AuthTokenFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthTokenFilter(authService));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registrationBean;
    }
}
