package io.lighting.config.example.embedded;

import io.lighting.config.embedded.EmbeddedConfigManager;
import io.lighting.config.embedded.EmbeddedConfigOptions;
import io.lighting.config.embedded.EmbeddedConfigOptions.StorageType;
import io.lighting.config.example.embedded.jpa.InventoryItem;
import io.lighting.config.example.embedded.jpa.InventoryItemRepository;
import io.lighting.config.example.embedded.mybatis.CustomerNote;
import io.lighting.config.example.embedded.mybatis.CustomerNoteService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@MapperScan("io.lighting.config.example.embedded.mybatis")
@io.lighting.config.embedded.EnableLightingEmbedded
public class EmbeddedDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddedDemoApplication.class, args);
    }

    @Bean(destroyMethod = "close")
    public EmbeddedConfigManager embeddedConfigManager(
            @Value("${lighting.config.embedded.storage.path:./example-config.json}") String storagePath) {
        return EmbeddedConfigManager.create(EmbeddedConfigOptions.builder()
                .storageType(StorageType.FILE)
                .storagePath(Paths.get(storagePath))
                .build());
    }

    @Bean
    CommandLineRunner demoDataInitializer(InventoryItemRepository repository,
                                         CustomerNoteService noteService) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                        createInventoryItem("SWITCH-100", "Edge Switch", new BigDecimal("499.00"), 6, "networking,edge"),
                        createInventoryItem("CAM-200", "Outdoor Camera", new BigDecimal("199.00"), 12, "security,poe")
                ));
            }

            if (noteService.count() == 0) {
                noteService.saveBatch(List.of(
                        createNote("acme", "Prefers deliveries before 10am"),
                        createNote("globex", "Route via backup DC during maintenance windows")
                ));
            }
        };
    }

    private InventoryItem createInventoryItem(String sku, String name, BigDecimal price, int stock, String tags) {
        InventoryItem item = new InventoryItem();
        item.setSku(sku);
        item.setName(name);
        item.setPrice(price);
        item.setStock(stock);
        item.setTags(tags);
        item.setCreatedAt(LocalDateTime.now());
        return item;
    }

    private CustomerNote createNote(String customer, String note) {
        CustomerNote entity = new CustomerNote();
        entity.setCustomer(customer);
        entity.setNote(note);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
