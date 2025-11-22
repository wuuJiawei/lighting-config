package io.lighting.config.server.rest;

import io.lighting.config.core.dto.ConfigSelector;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.util.TimeProvider;
import io.lighting.config.server.rest.dto.ConfigResponse;
import io.lighting.config.server.rest.dto.ConfigUpsertRequest;
import io.lighting.config.server.rest.dto.RollbackRequest;
import io.lighting.config.server.service.ConfigApplicationService;
import io.lighting.config.server.config.OpenApiConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import static io.lighting.config.server.web.ApiConstants.ADMIN_CONFIG_PATH;
import static io.lighting.config.server.web.ApiConstants.DEFAULT_ACTOR;
import static io.lighting.config.server.web.ApiConstants.DEFAULT_APP_ID;
import static io.lighting.config.server.web.ApiConstants.DEFAULT_NAMESPACE;
import static io.lighting.config.server.web.ApiConstants.DEFAULT_TENANT;

@RestController
@RequestMapping(ADMIN_CONFIG_PATH)
@Validated
@Tag(name = "Admin Config")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME)
public class ConsoleConfigController {

    private final ConfigApplicationService applicationService;
    private final TimeProvider timeProvider;

    public ConsoleConfigController(ConfigApplicationService applicationService, TimeProvider timeProvider) {
        this.applicationService = applicationService;
        this.timeProvider = timeProvider;
    }

    @GetMapping
    @Operation(summary = "Query configuration items", description = "Supports exact key lookup or prefix scanning.")
    public ResponseEntity<List<ConfigResponse>> query(
            @Parameter(description = "Tenant identifier", example = "default")
            @RequestParam(defaultValue = DEFAULT_TENANT) String tenant,
            @Parameter(description = "Namespace identifier", example = "default")
            @RequestParam(defaultValue = DEFAULT_NAMESPACE) String namespace,
            @Parameter(description = "App ID / scope", example = "default")
            @RequestParam(name = "appId", defaultValue = DEFAULT_APP_ID) String appId,
            @Parameter(description = "Exact key to fetch") @RequestParam(required = false) String key,
            @Parameter(description = "Prefix to filter keys") @RequestParam(required = false) String prefix) {
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
    @Operation(summary = "Create or update a configuration entry")
    public ResponseEntity<ConfigResponse> upsert(@Valid @RequestBody ConfigUpsertRequest request) {
        Instant now = timeProvider.now();
        ConfigItem saved = applicationService.upsert(request.toConfigItem(now), DEFAULT_ACTOR);
        return ResponseEntity.ok(new ConfigResponse(saved));
    }

    @PostMapping("/rollback")
    @Operation(summary = "Rollback a configuration entry to a historical version")
    public ResponseEntity<ConfigResponse> rollback(@Valid @RequestBody RollbackRequest request) {
        ConfigItem rolledBack = applicationService.rollback(
                request.getTenant(),
                request.getNamespace(),
                request.getAppId(),
                request.getKey(),
                request.getTargetVersion(),
                DEFAULT_ACTOR);
        return ResponseEntity.ok(new ConfigResponse(rolledBack));
    }

    @DeleteMapping
    @Operation(summary = "Delete a configuration entry")
    public ResponseEntity<Void> delete(
            @RequestParam(defaultValue = DEFAULT_TENANT) String tenant,
            @RequestParam(defaultValue = DEFAULT_NAMESPACE) String namespace,
            @RequestParam(name = "appId", defaultValue = DEFAULT_APP_ID) String appId,
            @RequestParam String key) {
        applicationService.delete(tenant, namespace, appId, key, DEFAULT_ACTOR);
        return ResponseEntity.noContent().build();
    }
}
