package com.hospital.mes.platform.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class Mes001OpenApiIT {
    @Autowired private MockMvc mvc;

    @Test
    void generatedContractContainsTheFrozenMes001Operations() throws Exception {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/api/v1/audit-events'].get.operationId")
                .value("queryAuditEvents"))
            .andExpect(jsonPath("$.paths['/api/v1/auth/reauth'].post.operationId")
                .value("reauthenticateForSignature"))
            .andExpect(jsonPath("$.paths['/api/v1/records/{type}/{id}/sign'].post.operationId")
                .value("signRecord"))
            .andExpect(jsonPath("$.paths['/api/v1/integration/messages'].get.operationId")
                .value("queryIntegrationMessages"))
            .andExpect(jsonPath("$.paths['/api/v1/integration/messages/{messageRef}/retry'].post.operationId")
                .value("retryIntegrationMessage"))
            .andExpect(jsonPath("$.components.schemas.IntegrationMessageResponse.properties.payloadJson")
                .doesNotExist());
    }
}
