package io.lighting.config.server.rest;

import io.lighting.config.server.config.OpenApiConfiguration;
import io.lighting.config.server.rest.dto.RevisionResponse;
import io.lighting.config.server.service.ConfigApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/lighting-config/api/revisions")
@Validated
@Tag(name = "Revisions")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME)
public class RevisionController {

    private final ConfigApplicationService applicationService;

    public RevisionController(ConfigApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    @Operation(summary = "List revisions for a configuration")
    public ResponseEntity<List<RevisionResponse>> list(
            @Parameter(description = "Tenant identifier", example = "default")
            @RequestParam(defaultValue = "default") String tenant,
            @Parameter(description = "Namespace identifier", example = "default")
            @RequestParam(defaultValue = "default") String namespace,
            @Parameter(description = "App ID / scope", example = "default")
            @RequestParam(name = "appId", defaultValue = "default") String appId,
            @Parameter(description = "Config key", required = true)
            @RequestParam String key) {
        List<RevisionResponse> payload = applicationService.listRevisions(tenant, namespace, appId, key).stream()
                .map(RevisionResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payload);
    }
}
