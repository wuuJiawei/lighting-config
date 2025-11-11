package io.lighting.config.example.embedded;

import io.lighting.config.embedded.EmbeddedConfigManager;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ContentType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/embedded/config")
public class EmbeddedConfigController {

    private final EmbeddedConfigManager manager;

    public EmbeddedConfigController(EmbeddedConfigManager manager) {
        this.manager = manager;
    }

    @GetMapping
    public List<ConfigItem> list(@RequestParam String tenant,
                                 @RequestParam String namespace,
                                 @RequestParam String appId,
                                 @RequestParam(required = false) String prefix) {
        return manager.list(tenant, namespace, appId, prefix);
    }

    @PostMapping
    public ResponseEntity<Void> upsert(@RequestBody Map<String, String> body) {
        manager.upsert(
                body.getOrDefault("tenant", "default"),
                body.getOrDefault("namespace", "default"),
                body.getOrDefault("appId", "demo-client"),
                body.get("key"),
                body.get("value"),
                ContentType.fromAlias(body.getOrDefault("contentType", "TEXT"))
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam String tenant,
                                       @RequestParam String namespace,
                                       @RequestParam String appId,
                                       @RequestParam String key) {
        manager.delete(tenant, namespace, appId, key);
        return ResponseEntity.noContent().build();
    }
}
