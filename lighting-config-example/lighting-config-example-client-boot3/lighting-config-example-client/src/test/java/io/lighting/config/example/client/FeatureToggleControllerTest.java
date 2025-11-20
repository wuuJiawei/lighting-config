package io.lighting.config.example.client;

import com.alibaba.fastjson2.JSONObject;
import io.lighting.config.core.dto.ConfigChange;
import io.lighting.config.core.model.ConfigCoordinate;
import io.lighting.config.core.model.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeatureToggleController.class)
class FeatureToggleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeatureToggleController controller;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "orderV2", true);
        ReflectionTestUtils.setField(controller, "jsonConfig", JSONObject.parse("{\"enabled\":true,\"ratio\":0.35}"));
    }

    @Test
    void orderFlagEndpointReflectsCurrentValue() throws Exception {
        mockMvc.perform(get("/feature/order"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void jsonConfigEndpointReturnsCurrentObject() throws Exception {
        mockMvc.perform(get("/feature/json-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.ratio").value(0.35));
    }

    @Test
    void lastEventEndpointReportsLatestListenerNotification() throws Exception {
        ConfigChange change = ConfigChange.builder()
                .coordinate(ConfigCoordinate.of("default", "default", "default", "feature.order.v2"))
                .contentType(ContentType.STRING)
                .value("true")
                .build();
        controller.onFeatureChange(change);

        mockMvc.perform(get("/events/last"))
                .andExpect(status().isOk())
                .andExpect(content().string("feature.order.v2=true"));
    }
}
