package io.lighting.config.example.server;

import io.lighting.config.core.dto.ConfigSelector;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.util.TimeProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@Profile("server")
@RequestMapping("/example/config")
public class SimpleConfigController {

    private final ConfigApplicationService applicationService;
    private final TimeProvider timeProvider;

    public SimpleConfigController(ConfigApplicationService applicationService,
                                  TimeProvider timeProvider) {
        this.applicationService = applicationService;
        this.timeProvider = timeProvider;
    }

    @GetMapping
    public ResponseEntity<List<ConfigItem>> query(@RequestParam String tenant,
                                                  @RequestParam String namespace,
                                                  @RequestParam("appId") String appId,
                                                  @RequestParam(required = false) String key,
                                                  @RequestParam(required = false) String prefix) {
        if (StringUtils.hasText(key)) {
            return applicationService.get(tenant, namespace, appId, key)
                    .map(item -> ResponseEntity.ok(List.of(item)))
                    .orElse(ResponseEntity.notFound().build());
        }
        PullQuery.Builder builder = PullQuery.builder()
                .tenant(tenant)
                .namespace(namespace)
                .appId(appId);
        if (StringUtils.hasText(prefix)) {
            builder.selector(ConfigSelector.byPrefix(prefix));
        }
        return ResponseEntity.ok(applicationService.list(builder.build()));
    }

    @PostMapping
    public ResponseEntity<ConfigItem> upsert(@RequestBody SimpleConfigRequest request) {
        Instant now = timeProvider.now();
        ConfigItem saved = applicationService.upsert(request.toConfigItem(now), "example-server");
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam String tenant,
                                       @RequestParam String namespace,
                                       @RequestParam("appId") String appId,
                                       @RequestParam String key) {
        applicationService.delete(tenant, namespace, appId, key, "example-server");
        return ResponseEntity.noContent().build();
    }
}
