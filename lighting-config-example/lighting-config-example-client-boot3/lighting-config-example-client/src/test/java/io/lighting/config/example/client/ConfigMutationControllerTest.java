package io.lighting.config.example.client;

import io.lighting.config.example.client.admin.ConfigAdminClient;
import io.lighting.config.example.client.admin.ConfigAdminResponse;
import io.lighting.config.example.client.admin.ConfigMutationRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfigMutationController.class)
class ConfigMutationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigAdminClient adminClient;

    @Test
    void upsertDelegatesToAdminClient() throws Exception {
        ConfigAdminResponse response = new ConfigAdminResponse();
        response.setKey("demo.key");
        response.setValue("demo");
        response.setContentType("STRING");
        response.setEnabled(true);
        response.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        response.setLabels(Map.of());
        given(adminClient.upsert(any(ConfigMutationRequest.class))).willReturn(response);

        mockMvc.perform(post("/examples/admin/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"demo.key\",\"value\":\"demo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("demo.key"))
                .andExpect(jsonPath("$.value").value("demo"));

        ArgumentCaptor<ConfigMutationRequest> captor = ArgumentCaptor.forClass(ConfigMutationRequest.class);
        verify(adminClient).upsert(captor.capture());
        assertThat(captor.getValue().getKey()).isEqualTo("demo.key");
    }

    @Test
    void getReturnsNotFoundWhenServerHasNoEntry() throws Exception {
        given(adminClient.fetch("unknown.key")).willReturn(Optional.empty());

        mockMvc.perform(get("/examples/admin/config/unknown.key"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReturnsServerPayloadWhenPresent() throws Exception {
        ConfigAdminResponse response = new ConfigAdminResponse();
        response.setKey("feature.debug");
        response.setValue("true");
        response.setContentType("STRING");
        response.setEnabled(true);
        given(adminClient.fetch("feature.debug")).willReturn(Optional.of(response));

        mockMvc.perform(get("/examples/admin/config/feature.debug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("feature.debug"))
                .andExpect(jsonPath("$.value").value("true"));
    }

    @Test
    void deleteDelegatesToAdminClient() throws Exception {
        doNothing().when(adminClient).delete(eq("feature.debug"));

        mockMvc.perform(delete("/examples/admin/config/feature.debug"))
                .andExpect(status().isNoContent());

        verify(adminClient).delete("feature.debug");
    }
}
