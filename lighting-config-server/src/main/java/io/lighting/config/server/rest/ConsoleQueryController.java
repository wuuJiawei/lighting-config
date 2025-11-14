package io.lighting.config.server.rest;

import io.lighting.config.server.rest.dto.AuditRecordResponse;
import io.lighting.config.server.rest.dto.CacheMissAlertResponse;
import io.lighting.config.server.rest.dto.DashboardStatView;
import io.lighting.config.server.rest.dto.NamespaceSummaryResponse;
import io.lighting.config.server.service.ConsoleQueryService;
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
public class ConsoleQueryController {

    private final ObjectProvider<ConsoleQueryService> consoleQueryService;

    public ConsoleQueryController(ObjectProvider<ConsoleQueryService> consoleQueryService) {
        this.consoleQueryService = consoleQueryService;
    }

    @GetMapping("/stats")
    public ResponseEntity<List<DashboardStatView>> stats(@RequestParam(defaultValue = "default") String tenant) {
        ConsoleQueryService service = consoleQueryService.getIfAvailable();
        if (service == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(service.fetchDashboardStats(tenant));
    }

    @GetMapping("/namespaces")
    public ResponseEntity<List<NamespaceSummaryResponse>> namespaces(@RequestParam(defaultValue = "default") String tenant) {
        ConsoleQueryService service = consoleQueryService.getIfAvailable();
        if (service == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(service.fetchNamespaces(tenant));
    }

    @GetMapping("/audit")
    public ResponseEntity<List<AuditRecordResponse>> audit(@RequestParam(defaultValue = "default") String tenant,
                                                           @RequestParam(defaultValue = "10") int limit) {
        ConsoleQueryService service = consoleQueryService.getIfAvailable();
        if (service == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return ResponseEntity.ok(service.fetchAuditTrail(tenant, safeLimit));
    }

    @GetMapping("/cache-miss")
    public ResponseEntity<List<CacheMissAlertResponse>> cacheMiss(@RequestParam(defaultValue = "default") String tenant,
                                                                  @RequestParam(defaultValue = "20") int limit) {
        ConsoleQueryService service = consoleQueryService.getIfAvailable();
        if (service == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return ResponseEntity.ok(service.fetchCacheMissAlerts(tenant, safeLimit));
    }
}
