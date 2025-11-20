package io.lighting.config.example.client;

import io.lighting.config.core.model.ContentType;
import io.lighting.config.example.client.admin.ConfigAdminClient;
import io.lighting.config.example.client.admin.ConfigAdminResponse;
import io.lighting.config.example.client.admin.ConfigMutationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/examples/admin/config")
@Validated
public class ConfigMutationController {

    private final ConfigAdminClient adminClient;

    public ConfigMutationController(ConfigAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    @PostMapping
    public ConfigAdminResponse upsert(@RequestBody ConfigMutationRequest request) {
        if (request.getContentType() == null || request.getContentType().isBlank()) {
            request.setContentType(ContentType.STRING.name());
        }
        return adminClient.upsert(request);
    }

    @GetMapping("/{key}")
    public ResponseEntity<ConfigAdminResponse> get(@PathVariable String key) {
        return adminClient.fetch(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        adminClient.delete(key);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
