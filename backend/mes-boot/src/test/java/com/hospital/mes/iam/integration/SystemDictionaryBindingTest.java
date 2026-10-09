package com.hospital.mes.iam.integration;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.system.api.SystemDictionaryController;
import com.hospital.mes.system.application.PageResult;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemDictionaryBindingTest {
    private SystemDictionaryController controller;
    private MockMvc mvc;

    @BeforeEach void setup() {
        controller = mock(SystemDictionaryController.class);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test void listBindsPaginationAndAllFilters() throws Exception {
        when(controller.list(anyInt(), anyInt(), nullable(String.class), nullable(String.class),
            nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class)))
            .thenReturn(ApiResponse.success(new PageResult<>(List.of(), 0, 0, 20), "test"));
        mvc.perform(get("/api/v1/dictionaries")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/dictionaries").param("page", "1").param("size", "10")
            .param("code", "EQUIPMENT_TYPE").param("name", "设备").param("kind", "BUSINESS")
            .param("status", "ACTIVE").param("createdFrom", "2026-10-01").param("createdTo", "2026-10-09"))
            .andExpect(status().isOk());
        verify(controller).list(0, 20, null, null, null, null, null, null);
        verify(controller).list(1, 10, "EQUIPMENT_TYPE", "设备", "BUSINESS", "ACTIVE", "2026-10-01", "2026-10-09");
    }

    @Test void detailsItemsAndOptionsBindIdentifiersAndHistoricalValue() throws Exception {
        when(controller.get("20")).thenReturn(ApiResponse.success(null, "test"));
        when(controller.items("20")).thenReturn(ApiResponse.success(List.of(), "test"));
        when(controller.options("EQUIPMENT_TYPE", "LEGACY")).thenReturn(ApiResponse.success(List.of(), "test"));
        mvc.perform(get("/api/v1/dictionaries/20")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/dictionaries/20/items")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/dictionary-options/EQUIPMENT_TYPE").param("selectedValue", "LEGACY"))
            .andExpect(status().isOk());
        verify(controller).get("20");
        verify(controller).items("20");
        verify(controller).options("EQUIPMENT_TYPE", "LEGACY");
    }
}
