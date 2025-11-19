package io.lighting.config.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI specification + Swagger UI configuration to expose the admin/client endpoints documented in docs/系统设计文档.md.
 */
@Configuration
public class OpenApiConfiguration {

    public static final String SECURITY_SCHEME = "lightingAuth";

    @Bean
    public OpenAPI lightingConfigOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("lighting-config REST API")
                        .description("Admin and client HTTP endpoints for lighting-config.")
                        .version("v1")
                        .contact(new Contact().name("lighting-config team").url("https://lighting-tech.io"))
                        .license(new License().name("Apache License 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("TOKEN")
                                .description("Reuse the lighting-config auth token via 'Authorization: Bearer <token>' or 'X-Lighting-Token' header.")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    }

    @Bean
    public GroupedOpenApi lightingApiGroup() {
        return GroupedOpenApi.builder()
                .group("lighting-config")
                .pathsToMatch("/lighting-config/api/**")
                .build();
    }
}
