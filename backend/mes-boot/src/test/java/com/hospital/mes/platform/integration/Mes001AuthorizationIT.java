package com.hospital.mes.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.session.SessionStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class Mes001AuthorizationIT {
    @Autowired private MockMvc mvc;
    @Autowired private SessionStore sessions;
    @Autowired private AccessTokenCodec tokens;
    private final List<String> sessionIds = new ArrayList<>();

    @AfterEach
    void cleanup() {
        sessionIds.forEach(sessions::revoke);
    }

    @Test
    void auditAndIntegrationQueriesUseTheirDistinctPermissions() throws Exception {
        String auditToken = issue(Set.of("audit:view"));
        String integrationToken = issue(Set.of("integration:view"));

        mvc.perform(get("/api/v1/audit-events").header("Authorization", "Bearer " + auditToken))
            .andExpect(status().isOk());
        mvc.perform(get("/api/v1/integration/messages").header("Authorization", "Bearer " + auditToken))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/integration/messages").header("Authorization", "Bearer " + integrationToken))
            .andExpect(status().isOk());
        mvc.perform(get("/api/v1/audit-events").header("Authorization", "Bearer " + integrationToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void manualRetryRequiresBothFrozenAuthorities() throws Exception {
        String viewOnly = issue(Set.of("integration:view"));
        String retryOnly = issue(Set.of("integration:retry"));
        String both = issue(Set.of("integration:view", "integration:retry"));

        performRetry(viewOnly).andExpect(status().isForbidden());
        performRetry(retryOnly).andExpect(status().isForbidden());
        performRetry(both).andExpect(result ->
            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
    }

    private org.springframework.test.web.servlet.ResultActions performRetry(String token) throws Exception {
        return mvc.perform(post("/api/v1/integration/messages/INVALID/retry")
            .header("Authorization", "Bearer " + token)
            .header("Idempotency-Key", "test-key")
            .header("If-Match", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"reason\":\"operator retry\"}"));
    }

    private String issue(Set<String> permissions) {
        var lease = sessions.create(new LoginSnapshot(
            998877L, 1L, "platform-test", "Platform Test", Set.of("SYSTEM_ADMIN"), permissions, false));
        sessionIds.add(lease.sessionId());
        return tokens.issue(998877L, lease.sessionId());
    }
}
