package io.lighting.config.example.embedded;

import io.lighting.config.embedded.EmbeddedConfigManager;
import io.lighting.config.embedded.EmbeddedConfigOptions;
import io.lighting.config.embedded.EmbeddedConfigOptions.StorageType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Paths;

@SpringBootApplication
@io.lighting.config.embedded.EnableLightingEmbedded
public class EmbeddedDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddedDemoApplication.class, args);
    }

    @Bean(destroyMethod = "close")
    public EmbeddedConfigManager embeddedConfigManager() {
        return EmbeddedConfigManager.create(EmbeddedConfigOptions.builder()
                .storageType(StorageType.FILE)
                .storagePath(Paths.get("./example-config.json"))
                .build());
    }
}
