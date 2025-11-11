package io.lighting.config.server.rest;

import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PollAdvice;
import io.lighting.config.core.dto.PollRequest;
import io.lighting.config.core.dto.PollResponse;
import io.lighting.config.core.dto.PullQuery;
import io.lighting.config.core.dto.ConfigSelector;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.ContentType;
import io.lighting.config.server.rest.dto.PollRequestPayload;
import io.lighting.config.server.service.ConfigApplicationService;
import io.lighting.config.server.notify.ChangeFeed;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoint providing polling responses for clients adopting the HTTP polling transport.
 */
@RestController
@RequestMapping("/api")
public class PollController {

    private final ConfigApplicationService applicationService;
    private final ChangeFeed changeFeed;
    private final Duration defaultInterval = Duration.ofSeconds(30);

    public PollController(ConfigApplicationService applicationService,
                          ChangeFeed changeFeed) {
        this.applicationService = applicationService;
        this.changeFeed = changeFeed;
    }

    @PostMapping("/poll")
    public PollResponse poll(@RequestBody PollRequestPayload payload) {
        PollRequest request = payload.toRequest();
        if (request.getLastVersion() == 0) {
            return snapshotResponse(request);
        }
        ChangeFeed.Batch batch = changeFeed.fetchSince(request, request.getLastVersion());
        return PollResponse.builder()
                .version(batch.getLastVersion())
                .items(batch.getChanges())
                .advice(PollAdvice.builder().nextInterval(defaultInterval).build())
                .serverTime(Instant.now())
                .build();
    }

    private PollResponse snapshotResponse(PollRequest request) {
        PullQuery.Builder builder = PullQuery.builder()
                .tenant(request.getTenant())
                .namespace(request.getNamespace())
                .appId(request.getAppId());
        if (!request.getPrefixes().isEmpty()) {
            builder.selector(ConfigSelector.byPrefix(request.getPrefixes().get(0)));
        }
        List<ConfigChange> snapshot = applicationService.list(builder.build()).stream()
                .map(this::toChange)
                .collect(Collectors.toList());
        long version = changeFeed.currentOffset();
        return PollResponse.builder()
                .version(version)
                .items(snapshot)
                .advice(PollAdvice.builder().nextInterval(defaultInterval).build())
                .serverTime(Instant.now())
                .build();
    }

    private ConfigChange toChange(ConfigItem item) {
        return ConfigChange.builder()
                .coordinate(ConfigCoordinate.of(item.getTenant(), item.getNamespace(), item.getAppId(), item.getKey()))
                .version(item.getVersion())
                .type(item.isEnabled() ? ChangeType.UPSERT : ChangeType.DELETE)
                .contentType(item.getContentType())
                .value(item.getValue())
                .deleted(!item.isEnabled())
                .occurredAt(item.getUpdatedAt())
                .build();
    }
}
