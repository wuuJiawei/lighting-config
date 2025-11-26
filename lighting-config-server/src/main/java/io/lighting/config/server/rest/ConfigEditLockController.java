package io.lighting.config.server.rest;

import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.server.config.OpenApiConfiguration;
import io.lighting.config.server.lock.ConfigEditLock;
import io.lighting.config.server.lock.ConfigEditLockNotifier;
import io.lighting.config.server.lock.ConfigEditLockService;
import io.lighting.config.server.lock.ConfigEditLockView;
import io.lighting.config.server.lock.LockEventType;
import io.lighting.config.server.lock.LockOwner;
import io.lighting.config.server.lock.LockOwnerResolver;
import io.lighting.config.server.rest.dto.ConfigLockRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

import static io.lighting.config.server.web.ApiConstants.ADMIN_CONFIG_LOCK_PATH;
import static io.lighting.config.server.web.ApiConstants.DEFAULT_APP_ID;
import static io.lighting.config.server.web.ApiConstants.DEFAULT_NAMESPACE;
import static io.lighting.config.server.web.ApiConstants.DEFAULT_TENANT;

@RestController
@RequestMapping(ADMIN_CONFIG_LOCK_PATH)
@Validated
@Tag(name = "Admin Config Lock")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME)
public class ConfigEditLockController {

    private final ConfigEditLockService lockService;
    private final ConfigEditLockNotifier notifier;
    private final LockOwnerResolver ownerResolver;

    public ConfigEditLockController(ConfigEditLockService lockService,
                                    ConfigEditLockNotifier notifier,
                                    LockOwnerResolver ownerResolver) {
        this.lockService = lockService;
        this.notifier = notifier;
        this.ownerResolver = ownerResolver;
    }

    @PostMapping
    @Operation(summary = "Acquire or refresh an edit lock for a configuration entry")
    public ResponseEntity<ConfigEditLockView> acquire(@Valid @RequestBody ConfigLockRequest request,
                                                      HttpServletRequest httpRequest) {
        LockOwner owner = ownerResolver.resolve(httpRequest, request.getOwnerName());
        ConfigEditLock lock = lockService.acquire(request.toCoordinate(), owner);
        return ResponseEntity.ok(ConfigEditLockView.from(lock, owner, LockEventType.ACQUIRED));
    }

    @PostMapping("/release")
    @Operation(summary = "Release the edit lock held by current session")
    public ResponseEntity<ConfigEditLockView> release(@Valid @RequestBody ConfigLockRequest request,
                                                      HttpServletRequest httpRequest) {
        LockOwner owner = ownerResolver.resolve(httpRequest, request.getOwnerName());
        boolean released = lockService.release(request.toCoordinate(), owner);
        if (released) {
            return ResponseEntity.ok(ConfigEditLockView.unlocked(request.toCoordinate(), owner, LockEventType.RELEASED));
        }
        Optional<ConfigEditLock> current = lockService.currentLock(request.toCoordinate());
        if (current.isPresent()) {
            return ResponseEntity.status(409).body(ConfigEditLockView.from(current.get(), owner, LockEventType.SNAPSHOT));
        }
        return ResponseEntity.ok(ConfigEditLockView.unlocked(request.toCoordinate(), owner, LockEventType.SNAPSHOT));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to edit lock changes for a configuration entry via SSE")
    public SseEmitter stream(@Parameter(description = "Tenant identifier", example = "default")
                             @RequestParam(defaultValue = DEFAULT_TENANT) String tenant,
                             @Parameter(description = "Namespace identifier", example = "default")
                             @RequestParam(defaultValue = DEFAULT_NAMESPACE) String namespace,
                             @Parameter(description = "App ID", example = "lighting-console")
                             @RequestParam(name = "appId", defaultValue = DEFAULT_APP_ID) String appId,
                             @Parameter(description = "Config key", required = true) @RequestParam String key,
                             HttpServletRequest httpRequest) {
        ConfigCoordinate coordinate = ConfigCoordinate.of(tenant, namespace, appId, key);
        LockOwner owner = ownerResolver.resolve(httpRequest);
        return notifier.watch(coordinate, owner);
    }
}
