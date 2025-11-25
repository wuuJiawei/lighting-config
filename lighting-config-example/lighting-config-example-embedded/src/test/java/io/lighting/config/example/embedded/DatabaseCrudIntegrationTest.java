package io.lighting.config.example.embedded;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lighting.config.example.embedded.jpa.InventoryItem;
import io.lighting.config.example.embedded.jpa.InventoryItemRepository;
import io.lighting.config.example.embedded.mybatis.CustomerNote;
import io.lighting.config.example.embedded.mybatis.CustomerNoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DatabaseCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryItemRepository itemRepository;

    @Autowired
    private CustomerNoteService noteService;

    @BeforeEach
    void cleanDatabase() {
        itemRepository.deleteAll();
        noteService.lambdaUpdate().remove();
    }

    @Test
    void jpaCrudEndpointsWork() throws Exception {
        Map<String, Object> payload = Map.of(
                "sku", "TEST-001",
                "name", "Test Fixture",
                "price", new BigDecimal("12.50"),
                "stock", 3,
                "tags", "lab,fixture"
        );

        MvcResult create = mockMvc.perform(post("/examples/jpa/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andReturn();

        InventoryItem created = objectMapper.readValue(create.getResponse().getContentAsString(), InventoryItem.class);

        mockMvc.perform(get("/examples/jpa/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(created.getId()))
                .andExpect(jsonPath("$[0].name").value("Test Fixture"));

        Map<String, Object> updatePayload = Map.of(
                "sku", "TEST-001",
                "name", "Updated Fixture",
                "price", new BigDecimal("15.00"),
                "stock", 7,
                "tags", "lab,fixture"
        );

        mockMvc.perform(put("/examples/jpa/items/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(7))
                .andExpect(jsonPath("$.name").value("Updated Fixture"));

        mockMvc.perform(delete("/examples/jpa/items/{id}", created.getId()))
                .andExpect(status().isNoContent());

        assertEquals(0L, itemRepository.count());
    }

    @Test
    void mybatisPlusCrudEndpointsWork() throws Exception {
        Map<String, Object> payload = Map.of(
                "customer", "acme",
                "note", "prefers async delivery"
        );

        MvcResult create = mockMvc.perform(post("/examples/mybatis/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customer").value("acme"))
                .andReturn();

        CustomerNote created = objectMapper.readValue(create.getResponse().getContentAsString(), CustomerNote.class);

        mockMvc.perform(get("/examples/mybatis/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(created.getId()))
                .andExpect(jsonPath("$[0].note").value("prefers async delivery"));

        Map<String, Object> updatePayload = Map.of(
                "customer", "acme",
                "note", "prefers async delivery, avoid 1am-6am"
        );

        mockMvc.perform(put("/examples/mybatis/notes/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("prefers async delivery, avoid 1am-6am"));

        mockMvc.perform(delete("/examples/mybatis/notes/{id}", created.getId()))
                .andExpect(status().isNoContent());

        assertEquals(0L, noteService.count());
    }
}
