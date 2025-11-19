package io.lighting.config.example.client.admin;

import io.lighting.config.spring.boot.autoconfigure.LightingClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Thin HTTP client calling the lighting-config server Admin API.
 */
@Component
public class ConfigAdminClient {

    private static final Logger log = LoggerFactory.getLogger(ConfigAdminClient.class);

    private final RestTemplate restTemplate;
    private final LightingClientProperties properties;
    private final URI configEndpoint;

    public ConfigAdminClient(RestTemplateBuilder builder, LightingClientProperties properties) {
        this.properties = properties;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .additionalInterceptors((request, body, execution) -> {
                    String token = properties.getAuthToken();
                    if (token != null && !token.isBlank()) {
                        request.getHeaders().setBearerAuth(token);
                        request.getHeaders().set("X-Lighting-Token", token);
                    }
                    return execution.execute(request, body);
                })
                .build();
        this.configEndpoint = UriComponentsBuilder
                .fromHttpUrl(properties.getServer().getAddress())
                .path("/lighting-config/api/admin/config")
                .build()
                .toUri();
    }

    public ConfigAdminResponse upsert(ConfigMutationRequest mutation) {
        ConfigUpsertPayload payload = ConfigUpsertPayload.from(mutation, properties);
        ResponseEntity<ConfigAdminResponse> response =
                restTemplate.postForEntity(configEndpoint, payload, ConfigAdminResponse.class);
        Assert.state(response.getBody() != null, "Upsert response body is required");
        log.info("Config upserted via Admin API | key={} | appId={} | tenant={}",
                payload.getKey(), payload.getAppId(), payload.getTenant());
        return response.getBody();
    }

    public Optional<ConfigAdminResponse> fetch(String key) {
        URI uri = baseQueryUri()
                .queryParam("key", key)
                .build()
                .toUri();
        ResponseEntity<ConfigAdminResponse[]> response = restTemplate.getForEntity(uri, ConfigAdminResponse[].class);
        ConfigAdminResponse[] body = response.getBody();
        if (body == null || body.length == 0) {
            return Optional.empty();
        }
        return Optional.of(body[0]);
    }

    public void delete(String key) {
        URI uri = baseQueryUri()
                .queryParam("key", key)
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        log.info("Config deleted via Admin API | key={} | status={}", key, response.getStatusCode());
    }

    private UriComponentsBuilder baseQueryUri() {
        return UriComponentsBuilder.fromUri(configEndpoint)
                .replaceQueryParam("tenant", properties.getTenant())
                .replaceQueryParam("namespace", properties.getNamespace())
                .replaceQueryParam("appId", properties.getAppId());
    }

    private static final class ConfigUpsertPayload {
        private final String tenant;
        private final String namespace;
        private final String appId;
        private final String key;
        private final String value;
        private final String contentType;
        private final boolean enabled;
        private final Map<String, String> labels;

        private ConfigUpsertPayload(String tenant,
                                    String namespace,
                                    String appId,
                                    String key,
                                    String value,
                                    String contentType,
                                    boolean enabled,
                                    Map<String, String> labels) {
            this.tenant = tenant;
            this.namespace = namespace;
            this.appId = appId;
            this.key = key;
            this.value = value;
            this.contentType = contentType;
            this.enabled = enabled;
            this.labels = labels;
        }

        private static ConfigUpsertPayload from(ConfigMutationRequest mutation,
                                                LightingClientProperties properties) {
            return new ConfigUpsertPayload(
                    properties.getTenant(),
                    properties.getNamespace(),
                    properties.getAppId(),
                    mutation.getKey(),
                    mutation.getValue(),
                    mutation.getContentType(),
                    mutation.isEnabled(),
                    mutation.getLabels());
        }

        public String getTenant() {
            return tenant;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getAppId() {
            return appId;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getContentType() {
            return contentType;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Map<String, String> getLabels() {
            return labels;
        }
    }
}
