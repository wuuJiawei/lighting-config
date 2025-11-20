package io.lighting.config.example.client.admin;

import io.lighting.config.spring.boot.autoconfigure.LightingClientProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ConfigAdminClientTest {

    private ConfigAdminClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        LightingClientProperties properties = new LightingClientProperties();
        properties.setTenant("demo");
        properties.setNamespace("default");
        properties.setAppId("order-service");
        properties.setAuthToken("demo-token");
        properties.getServer().setAddress("http://localhost:9999");

        RestTemplateBuilder builder = new RestTemplateBuilder();
        client = new ConfigAdminClient(builder, properties);

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        assert restTemplate != null;
        this.server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    void upsertSendsTenantScopedPayload() {
        server.expect(requestTo("http://localhost:9999/lighting-config/api/admin/config"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer demo-token"))
                .andExpect(jsonPath("$.tenant").value("demo"))
                .andExpect(jsonPath("$.appId").value("order-service"))
                .andExpect(jsonPath("$.key").value("sample.key"))
                .andRespond(withSuccess("{\"key\":\"sample.key\",\"value\":\"demo\",\"contentType\":\"STRING\",\"enabled\":true}",
                        MediaType.APPLICATION_JSON));

        ConfigMutationRequest request = new ConfigMutationRequest();
        request.setKey("sample.key");
        request.setValue("demo");

        ConfigAdminResponse response = client.upsert(request);
        assertThat(response.getKey()).isEqualTo("sample.key");
        server.verify();
    }

    @Test
    void fetchParsesArrayPayload() {
        server.expect(requestTo("http://localhost:9999/lighting-config/api/admin/config?tenant=demo&namespace=default&appId=order-service&key=feature.test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"key\":\"feature.test\",\"value\":\"true\",\"contentType\":\"STRING\",\"enabled\":true}]",
                        MediaType.APPLICATION_JSON));

        Optional<ConfigAdminResponse> response = client.fetch("feature.test");
        assertThat(response).isPresent();
        assertThat(response.get().getValue()).isEqualTo("true");
        server.verify();
    }

    @Test
    void deleteInvokesServerApi() {
        server.expect(requestTo("http://localhost:9999/lighting-config/api/admin/config?tenant=demo&namespace=default&appId=order-service&key=feature.test"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        client.delete("feature.test");
        server.verify();
    }
}
