package io.lighting.config.server.rest;

import io.lighting.config.server.config.OpenApiConfiguration;
import io.lighting.config.server.rest.dto.AuditRecordResponse;
import io.lighting.config.server.rest.dto.CacheMissAlertResponse;
import io.lighting.config.server.rest.dto.DashboardStatView;
import io.lighting.config.server.rest.dto.NamespaceSummaryResponse;
import io.lighting.config.server.service.ConsoleQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/lighting-config/api/admin/console")
@Tag(name = "Admin Console")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME)
public class ConsoleQueryController {

    private final ObjectProvider<ConsoleQueryService> consoleQueryService;

    public ConsoleQueryController(ObjectProvider<ConsoleQueryService> consoleQueryService) {
        this.consoleQueryService = consoleQueryService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Dashboard stats for a tenant")
    public ResponseEntity<List<DashboardStatView>> stats(
            @Parameter(description = "Tenant identifier", example = "default")
            @RequestParam(defaultValue = "default") String tenant) {
        ConsoleQueryService service = consoleQueryService.getIfAvailable();
        if (service == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(service.fetchDashboardStats(tenant));
    }

    @GetMapping("/namespaces")
    @Operation(summary = "List namespaces for a tenant")
    public ResponseEntity<List<NamespaceSummaryResponse>> namespaces(
            @Parameter(description = "Tenant identifier", example = "default")
            @RequestParam(defaultValue = "default") String tenant) {
        ConsoleQueryService service = consoleQueryService.getIfAvailable();
        if (service == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(service.fetchNamespaces(tenant));
    }

    @GetMapping("/audit")
    @Operation(summary = "Recent configuration audit records")
    public ResponseEntity<List<AuditRecordResponse>> audit(
            @Parameter(description = "Tenant identifier", example = "default")
            @RequestParam(defaultValue = "default") String tenant,
            @Parameter(description = "Max results (1-100)", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        ConsoleQueryService service = consoleQueryService.getIfAvailable();
        if (service == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return ResponseEntity.ok(service.fetchAuditTrail(tenant, safeLimit));
    }

    @GetMapping("/cache-miss")
    @Operation(summary = "Recent cache miss alerts")
    public ResponseEntity<List<CacheMissAlertResponse>> cacheMiss(
            @Parameter(description = "Tenant identifier", example = "default")
            @RequestParam(defaultValue = "default") String tenant,
            @Parameter(description = "Max results (1-100)", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        ConsoleQueryService service = consoleQueryService.getIfAvailable();
        if (service == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return ResponseEntity.ok(service.fetchCacheMissAlerts(tenant, safeLimit));
    }
}
