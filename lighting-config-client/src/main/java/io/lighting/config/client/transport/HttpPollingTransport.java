package io.lighting.config.client.transport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lighting.config.client.config.ClientOptions;
import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PollAdvice;
import io.lighting.config.core.dto.PollRequest;
import io.lighting.config.core.dto.PollResponse;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ContentType;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple HTTP transport that fetches config snapshots via REST endpoints and
 * produces {@link PollResponse} objects. Until a dedicated polling endpoint is exposed
 * by the server, it leverages the existing /api/config list API.
 */
public class HttpPollingTransport implements PollingTransport {

    private static final Logger log = LoggerFactory.getLogger(HttpPollingTransport.class);
    private static final TypeReference<List<RestConfigItem>> LIST_TYPE = new TypeReference<>() {};

    private final ClientOptions options;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI baseUri;
    private final Duration requestTimeout;

    public HttpPollingTransport(ClientOptions options) {
        this.options = options;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.baseUri = normalizeBase(options.getServerAddress());
        this.requestTimeout = Duration.ofSeconds(5);
    }

    @Override
    public PollResponse poll(PollRequest request) {
        try {
            URI uri = buildUri(request);
            HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                    .timeout(requestTimeout)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Polling request failed with status " + response.statusCode());
            }
            List<RestConfigItem> payload = objectMapper.readValue(response.body(), LIST_TYPE);
            List<ConfigChange> changes = new ArrayList<>();
            long maxVersion = request.getLastVersion();
            for (RestConfigItem item : payload) {
                changes.add(item.toChange());
                maxVersion = Math.max(maxVersion, item.version);
            }
            return PollResponse.builder()
                    .version(maxVersion)
                    .items(changes)
                    .advice(PollAdvice.builder().nextInterval(options.getPollInterval()).build())
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Polling interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to execute polling request", e);
        }
    }

    private URI buildUri(PollRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(baseUri).append("/api/config")
                .append("?tenant=").append(encode(request.getTenant()))
                .append("&namespace=").append(encode(request.getNamespace()))
                .append("&appId=").append(encode(request.getAppId()));
        if (!request.getPrefixes().isEmpty()) {
            builder.append("&prefix=").append(encode(request.getPrefixes().get(0)));
        }
        return URI.create(builder.toString());
    }

    private static URI normalizeBase(String address) {
        if (address.endsWith("/")) {
            return URI.create(address.substring(0, address.length() - 1));
        }
        return URI.create(address);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static final class RestConfigItem {
        public String tenant;
        public String namespace;
        public String appId;
        public String key;
        public String value;
        public String contentType;
        public long version;
        public boolean enabled;

        ConfigChange toChange() {
            return ConfigChange.builder()
                    .coordinate(ConfigCoordinate.of(tenant, namespace, appId, key))
                    .version(version)
                    .type(ChangeType.UPSERT)
                    .contentType(ContentType.fromAlias(contentType))
                    .value(value)
                    .deleted(false)
                    .occurredAt(java.time.Instant.now())
                    .build();
        }
    }
}
