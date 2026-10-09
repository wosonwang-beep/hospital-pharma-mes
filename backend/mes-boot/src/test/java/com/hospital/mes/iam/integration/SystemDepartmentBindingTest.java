package com.hospital.mes.iam.integration;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.system.api.SystemDepartmentController;
import com.hospital.mes.system.application.PageResult;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemDepartmentBindingTest {
    private SystemDepartmentController controller;
    private MockMvc mvc;

    @BeforeEach void setup() {
        controller = mock(SystemDepartmentController.class);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test void leaderOptionsAcceptMissingAndProvidedKeyword() throws Exception {
        when(controller.people(null)).thenReturn(ApiResponse.success(List.of(), "test"));
        when(controller.people("admin")).thenReturn(ApiResponse.success(List.of(), "test"));
        mvc.perform(get("/api/v1/department-users")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/department-users").param("keyword", "admin")).andExpect(status().isOk());
        verify(controller).people(null);
        verify(controller).people("admin");
    }

    @Test void listBindsDefaultPaginationAndNamedFilters() throws Exception {
        when(controller.list(anyInt(), anyInt(), nullable(String.class), nullable(String.class), nullable(String.class)))
            .thenReturn(ApiResponse.success(new PageResult<>(List.of(), 0, 0, 20), "test"));
        mvc.perform(get("/api/v1/departments")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/departments").param("page", "1").param("size", "10")
            .param("code", "QA").param("name", "质量").param("status", "ACTIVE"))
            .andExpect(status().isOk());
        verify(controller).list(0, 20, null, null, null);
        verify(controller).list(1, 10, "QA", "质量", "ACTIVE");
    }

    @Test void detailsBindDepartmentAndUserIdentifiers() throws Exception {
        when(controller.get("20")).thenReturn(ApiResponse.success(null, "test"));
        when(controller.getAssignment("1")).thenReturn(ApiResponse.success(
            new SystemDepartmentController.UserDepartment("1", null, null, 0), "test"));
        mvc.perform(get("/api/v1/departments/20")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/users/1/department")).andExpect(status().isOk());
        verify(controller).get("20");
        verify(controller).getAssignment("1");
    }
}
