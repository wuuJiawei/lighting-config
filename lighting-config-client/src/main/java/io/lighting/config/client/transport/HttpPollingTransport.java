package io.lighting.config.client.transport;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lighting.config.client.config.ClientOptions;
import io.lighting.config.core.dto.ChangeType;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.dto.PollAdvice;
import io.lighting.config.core.dto.PollRequest;
import io.lighting.config.core.dto.PollResponse;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HTTP-based polling transport that talks to the `/api/poll` endpoint.
 */
public class HttpPollingTransport implements PollingTransport {

    private static final Logger log = LoggerFactory.getLogger(HttpPollingTransport.class);
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
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.baseUri = normalizeBase(options.getServerAddress());
        this.requestTimeout = Duration.ofSeconds(5);
    }

    @Override
    public PollResponse poll(PollRequest request) {
        try {
            URI uri = baseUri.resolve("/lighting-config/api/poll");
            String body = objectMapper.writeValueAsString(request);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
                    .timeout(requestTimeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
            applyAuth(requestBuilder);
            HttpRequest httpRequest = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Polling request failed with status " + response.statusCode());
            }
            RestPollResponse payload = objectMapper.readValue(response.body(), RestPollResponse.class);
            return payload.toDomain(options.getPollInterval());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Polling interrupted", e);
        } catch (IOException e) {
            log.error("Polling failed", e);
            throw new IllegalStateException("Failed to execute polling request", e);
        }
    }

    private static URI normalizeBase(String address) {
        if (address.endsWith("/")) {
            return URI.create(address.substring(0, address.length() - 1));
        }
        return URI.create(address);
    }

    private void applyAuth(HttpRequest.Builder builder) {
        String token = options.getAuthToken();
        if (token == null || token.isBlank()) {
            return;
        }
        String value = token.trim();
        builder.header("Authorization", "Bearer " + value);
        builder.header("X-Lighting-Token", value);
    }

    private static final class RestPollResponse {
        public long version;
        public List<RestConfigChange> items = List.of();
        public RestAdvice advice;
        public Long serverTime;

        PollResponse toDomain(Duration fallbackInterval) {
            PollAdvice domainAdvice = null;
            if (advice != null) {
                Duration next = advice.nextInterval != null ? advice.nextInterval : fallbackInterval;
                domainAdvice = PollAdvice.builder()
                        .nextInterval(next)
                        .throttled(advice.throttled)
                        .build();
            }
            return PollResponse.builder()
                    .version(version)
                    .items(items.stream().map(RestConfigChange::toChange).collect(Collectors.toList()))
                    .advice(domainAdvice)
                    .serverTime(serverTime != null ? serverTime : System.currentTimeMillis())
                    .build();
        }
    }

    private static final class RestAdvice {
        public Duration nextInterval;
        public boolean throttled;
    }

    private static final class RestConfigChange {
        public RestCoordinate coordinate;
        public long version;
        public String contentType;
        public String value;
        public boolean deleted;
        public Long occurredAt;

        ConfigChange toChange() {
            return ConfigChange.builder()
                    .coordinate(coordinate.toCoordinate())
                    .version(version)
                    .type(deleted ? ChangeType.DELETE : ChangeType.UPSERT)
                    .contentType(ContentType.fromAlias(contentType))
                    .value(value)
                    .deleted(deleted)
                    .occurredAt(occurredAt != null ? occurredAt : System.currentTimeMillis())
                    .build();
        }
    }

    private static final class RestCoordinate {
        public String tenant;
        public String namespace;
        public String appId;
        public String key;

        ConfigCoordinate toCoordinate() {
            return ConfigCoordinate.of(tenant, namespace, appId, key);
        }
    }
}
