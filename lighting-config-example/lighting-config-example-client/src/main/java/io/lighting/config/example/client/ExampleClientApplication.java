package io.lighting.config.example.client;

import io.lighting.config.spring.boot.annotation.EnableLightingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableLightingConfig
public class ExampleClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExampleClientApplication.class, args);
    }
}
