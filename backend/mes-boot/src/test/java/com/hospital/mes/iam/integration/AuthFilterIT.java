package com.hospital.mes.iam.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.session.SessionStore;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class AuthFilterIT {
    @Autowired private MockMvc mvc;
    @Autowired private SessionStore sessions;
    @Autowired private AccessTokenCodec tokens;
    private String sessionId;

    @AfterEach void cleanup() { if (sessionId != null) sessions.revoke(sessionId); }

    @Test
    void missingAndAlteredBearerTokensAreUnauthorizedWithTrace() throws Exception {
        mvc.perform(get("/api/v1/auth/me").header("X-Trace-Id", "auth-test-trace"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.traceId").value("auth-test-trace"));
        String token = issue(7, Set.of("menu:home"));
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token + "x"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void revokedOrMismatchedRedisSessionRejectsOtherwiseValidJwt() throws Exception {
        String token = issue(7, Set.of("menu:home"));
        sessions.revoke(sessionId);
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
        issue(7, Set.of("menu:home"));
        String wrongSubject = tokens.issue(8, sessionId);
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + wrongSubject))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownAdminRouteIsDeniedEvenForAuthenticatedUser() throws Exception {
        String token = issue(7, Set.of("menu:home"));
        mvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/foundation/status")).andExpect(status().isOk());
    }

    private String issue(long userId, Set<String> permissions) {
        var lease = sessions.create(new LoginSnapshot(userId, "staff", "Staff",
            Set.of("STAFF"), permissions, false));
        sessionId = lease.sessionId();
        return tokens.issue(userId, sessionId);
    }
}
