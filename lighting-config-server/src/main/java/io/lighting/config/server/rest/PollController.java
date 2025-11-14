package io.lighting.config.server.rest;

import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PollAdvice;
import io.lighting.config.core.dto.PollRequest;
import io.lighting.config.core.dto.PollResponse;
import io.lighting.config.server.rest.dto.PollRequestPayload;
import io.lighting.config.server.cache.ClientSnapshotService;
import io.lighting.config.server.notify.ChangeFeed;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

/**
 * REST endpoint providing polling responses for clients adopting the HTTP polling transport.
 */
@RestController
@RequestMapping("/lighting-config/api")
public class PollController {

    private final ClientSnapshotService clientSnapshotService;
    private final ChangeFeed changeFeed;
    private final Duration defaultInterval = Duration.ofSeconds(30);

    public PollController(ClientSnapshotService clientSnapshotService,
                          ChangeFeed changeFeed) {
        this.clientSnapshotService = clientSnapshotService;
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
                .serverTime(System.currentTimeMillis())
                .build();
    }

    private PollResponse snapshotResponse(PollRequest request) {
        List<ConfigChange> snapshot = clientSnapshotService.snapshot(request);
        long version = changeFeed.currentOffset();
        return PollResponse.builder()
                .version(version)
                .items(snapshot)
                .advice(PollAdvice.builder().nextInterval(defaultInterval).build())
                .serverTime(System.currentTimeMillis())
                .build();
    }
}
