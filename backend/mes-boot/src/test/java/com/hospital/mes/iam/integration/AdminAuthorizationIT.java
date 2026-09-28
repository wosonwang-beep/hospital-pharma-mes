package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class AdminAuthorizationIT {
    @Autowired private MockMvc mvc;
    @Autowired private SessionStore sessions;
    @Autowired private AccessTokenCodec tokens;
    private final List<String> sessionIds = new ArrayList<>();

    @AfterEach
    void cleanup() { sessionIds.forEach(sessions::revoke); }

    @Test
    void menuOnlyCanReadButCannotMutate() throws Exception {
        String token = issue(Set.of("menu:iam:users"), false);
        mvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + token))
            .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        mvc.perform(post("/api/v1/admin/users").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void actionWithoutMenuIsForbidden() throws Exception {
        String token = issue(Set.of("action:iam:user.manage"), false);
        mvc.perform(post("/api/v1/admin/users").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void roleMenuOnlyCanReadButCannotMutate() throws Exception {
        String token = issue(Set.of("menu:iam:roles"), false);
        mvc.perform(get("/api/v1/admin/roles").header("Authorization", "Bearer " + token))
            .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        mvc.perform(post("/api/v1/admin/roles").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void unknownAdminPathIsForbidden() throws Exception {
        String token = issue(Set.of("menu:iam:users", "menu:iam:roles",
            "action:iam:user.manage", "action:iam:role.manage"), false);
        mvc.perform(get("/api/v1/admin/unknown").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void missingAndInvalidTokensAreUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/admin/users").header("X-Trace-Id", "admin-auth-trace"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.traceId").value("admin-auth-trace"));
        String token = issue(Set.of("menu:iam:users"), false);
        mvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + token + "x"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void mustChangePasswordCannotUseAdminRoutes() throws Exception {
        String token = issue(Set.of("menu:iam:users", "action:iam:user.manage"), true);
        mvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    private String issue(Set<String> permissions, boolean mustChangePassword) {
        var lease = sessions.create(new LoginSnapshot(998877L, "admin", "Admin",
            Set.of("SYSTEM_ADMIN"), permissions, mustChangePassword));
        sessionIds.add(lease.sessionId());
        return tokens.issue(998877L, lease.sessionId());
    }
}
