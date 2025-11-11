package io.lighting.config.embedded;

import io.lighting.config.core.model.ContentType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddedConfigManagerTest {

    @Test
    void listenersReceiveUpdates() throws InterruptedException {
        EmbeddedConfigManager manager = EmbeddedConfigManager.create(EmbeddedConfigOptions.builder().build());
        CountDownLatch latch = new CountDownLatch(1);
        manager.addListener(change -> {
            if ("feature.flag".equals(change.getCoordinate().getKey())) {
                latch.countDown();
            }
        });

        manager.upsert("default", "default", "demo", "feature.flag", "true", ContentType.TEXT);
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals("true", manager.get("default", "default", "demo", "feature.flag").orElseThrow().getValue());
    }

    @Test
    void fileStoragePersists() throws Exception {
        Path tempFile = Files.createTempFile("lighting-config", ".json");
        EmbeddedConfigOptions options = EmbeddedConfigOptions.builder()
                .storageType(EmbeddedConfigOptions.StorageType.FILE)
                .storagePath(tempFile)
                .build();
        try (EmbeddedConfigManager manager = EmbeddedConfigManager.create(options)) {
            manager.upsert("default", "prod", "payments", "db.url", "jdbc:h2", ContentType.TEXT);
        }

        EmbeddedConfigManager reloaded = EmbeddedConfigManager.create(options);
        assertEquals("jdbc:h2", reloaded.get("default", "prod", "payments", "db.url").orElseThrow().getValue());
    }
}
