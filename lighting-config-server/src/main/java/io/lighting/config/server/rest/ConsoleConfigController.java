package io.lighting.config.server.rest;

import io.lighting.config.core.dto.ConfigSelector;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.util.TimeProvider;
import io.lighting.config.server.rest.dto.ConfigResponse;
import io.lighting.config.server.rest.dto.ConfigUpsertRequest;
import io.lighting.config.server.service.ConfigApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/lighting-config/api/admin/config")
@Validated
public class ConsoleConfigController {

    private final ConfigApplicationService applicationService;
    private final TimeProvider timeProvider;

    public ConsoleConfigController(ConfigApplicationService applicationService, TimeProvider timeProvider) {
        this.applicationService = applicationService;
        this.timeProvider = timeProvider;
    }

    @GetMapping
    public ResponseEntity<List<ConfigResponse>> query(@RequestParam(defaultValue = "default") String tenant,
                                                      @RequestParam(defaultValue = "default") String namespace,
                                                      @RequestParam(name = "appId", defaultValue = "default") String appId,
                                                      @RequestParam(required = false) String key,
                                                      @RequestParam(required = false) String prefix) {
        if (key != null && !key.isEmpty()) {
            return applicationService.get(tenant, namespace, appId, key)
                    .map(ConfigResponse::new)
                    .map(single -> ResponseEntity.ok(List.of(single)))
                    .orElse(ResponseEntity.notFound().build());
        }
        PullQuery.Builder builder = PullQuery.builder()
                .tenant(tenant)
                .namespace(namespace)
                .appId(appId);
        if (prefix != null && !prefix.isEmpty()) {
            builder.selector(ConfigSelector.byPrefix(prefix));
        }
        List<ConfigResponse> responses = applicationService.list(builder.build()).stream()
                .map(ConfigResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ConfigResponse> upsert(@Valid @RequestBody ConfigUpsertRequest request) {
        Instant now = timeProvider.now();
        ConfigItem saved = applicationService.upsert(request.toConfigItem(now), "api");
        return ResponseEntity.ok(new ConfigResponse(saved));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam(defaultValue = "default") String tenant,
                                       @RequestParam(defaultValue = "default") String namespace,
                                       @RequestParam(name = "appId", defaultValue = "default") String appId,
                                       @RequestParam String key) {
        applicationService.delete(tenant, namespace, appId, key, "api");
        return ResponseEntity.noContent().build();
    }
}
