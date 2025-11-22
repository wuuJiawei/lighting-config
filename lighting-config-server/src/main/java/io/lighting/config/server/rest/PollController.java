package io.lighting.config.server.rest;

import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PollRequest;
import io.lighting.config.core.dto.PollResponse;
import io.lighting.config.server.rest.dto.PollRequestPayload;
import io.lighting.config.server.cache.ClientSnapshotService;
import io.lighting.config.server.notify.ChangeFeed;
import io.lighting.config.server.config.OpenApiConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.lighting.config.server.web.ApiConstants.API_BASE_PATH;

/**
 * REST endpoint providing polling responses for clients adopting the HTTP polling transport.
 */
@RestController
@RequestMapping(API_BASE_PATH)
@Tag(name = "Client Polling")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME)
public class PollController {

    private final ClientSnapshotService clientSnapshotService;
    private final ChangeFeed changeFeed;

    public PollController(ClientSnapshotService clientSnapshotService,
                          ChangeFeed changeFeed) {
        this.clientSnapshotService = clientSnapshotService;
        this.changeFeed = changeFeed;
    }

    @PostMapping("/poll")
    @Operation(summary = "Incremental poll endpoint for lighting-config clients")
    public PollResponse poll(@RequestBody PollRequestPayload payload) {
        PollRequest request = payload.toRequest();
        if (request.getLastVersion() == 0) {
            return snapshotResponse(request);
        }
        ChangeFeed.Batch batch = changeFeed.fetchSince(request, request.getLastVersion());
        return PollResponse.builder()
                .version(batch.getLastVersion())
                .items(batch.getChanges())
                .serverTime(System.currentTimeMillis())
                .build();
    }

    private PollResponse snapshotResponse(PollRequest request) {
        List<ConfigChange> snapshot = clientSnapshotService.snapshot(request);
        long version = changeFeed.currentOffset();
        return PollResponse.builder()
                .version(version)
                .items(snapshot)
                .serverTime(System.currentTimeMillis())
                .build();
    }
}
