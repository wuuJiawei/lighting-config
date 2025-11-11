package io.lighting.config.embedded.web;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "io.lighting.config.server.rest")
public class EmbeddedRestMvcConfiguration {
}
