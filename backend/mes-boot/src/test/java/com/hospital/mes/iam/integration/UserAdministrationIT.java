package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.session.SessionStore;
import com.hospital.mes.system.application.AdminSecurityEventWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
@Transactional
class UserAdministrationIT {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private SessionStore sessions;
    @Autowired private AccessTokenCodec tokens;
    @MockitoSpyBean private SessionStore sessionSpy;
    @MockitoSpyBean private AdminSecurityEventWriter eventSpy;
    private final List<String> leases = new ArrayList<>();

    @AfterEach void cleanup() {
        reset(sessionSpy, eventSpy);
        leases.forEach(sessions::revoke);
    }

    @Test
    void createReturnsOneTimeSecretWithoutPersistingOrAuditingIt() throws Exception {
        String login = "staff" + suffix();
        String token = administrator();
        var response = mvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("loginName", login, "displayName", "Staff Name"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.user.loginName").value(login))
            .andReturn().getResponse();
        assertThat(response.getHeader("Cache-Control")).contains("no-store");
        JsonNode body = json.readTree(response.getContentAsString());
        String secret = body.at("/data/temporaryPassword").asText();
        assertThat(secret).hasSize(24);
        String hash = jdbc.queryForObject("SELECT password_hash FROM sys_user WHERE login_name = ?", String.class, login);
        assertThat(hash).isNotEqualTo(secret);
        assertThat(jdbc.queryForObject("SELECT must_change_password FROM sys_user WHERE login_name = ?", Boolean.class, login)).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_security_event WHERE event_type = 'USER_CREATED' AND actor_user_id = 998877", Integer.class)).isPositive();
        assertThat(jdbc.queryForList("SELECT request_context FROM sys_security_event WHERE event_type = 'USER_CREATED'", String.class))
            .allSatisfy(context -> assertThat(context == null ? "" : context).doesNotContain(secret));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void failedEventWriteRollsBackCreatedUser() throws Exception {
        String login = "staff" + suffix();
        try {
            doThrow(new IllegalStateException("test event failure")).when(eventSpy)
                .append(eq("USER_CREATED"), any(), any(), nullable(Long.class), nullable(Long.class), any());
            mvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(administrator()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(Map.of("loginName", login, "displayName", "Staff"))))
                .andExpect(status().isInternalServerError());
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE login_name = ?", Integer.class, login))
                .isZero();
        } finally {
            jdbc.update("DELETE FROM sys_user WHERE login_name = ?", login);
        }
    }

    @Test
    void duplicateNormalizedLoginConflicts() throws Exception {
        String login = "staff" + suffix();
        String token = administrator();
        mvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("loginName", login, "displayName", "First"))))
            .andExpect(status().isOk());
        mvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("loginName", login.toUpperCase(), "displayName", "Second"))))
            .andExpect(status().isConflict());
    }

    @Test
    void profileAndStateChangesUseExpectedVersion() throws Exception {
        effectiveAdmin();
        long userId = user("staff" + suffix());
        String token = administrator();
        mvc.perform(patch("/api/v1/admin/users/{userId}/profile", userId)
                .header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Renamed\",\"expectedVersion\":0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.displayName").value("Renamed"));
        mvc.perform(patch("/api/v1/admin/users/{userId}/profile", userId)
                .header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Stale\",\"expectedVersion\":0}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/api/v1/admin/users/{userId}/disable", userId)
                .header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"expectedVersion\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.enabled").value(false));
        assertThat(jdbc.queryForObject("SELECT enabled FROM sys_user WHERE id = ?", Boolean.class, userId)).isFalse();
        mvc.perform(post("/api/v1/admin/users/{userId}/enable", userId)
                .header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"expectedVersion\":2}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.enabled").value(true));
        mvc.perform(get("/api/v1/admin/users/{userId}", userId)
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.displayName").value("Renamed"))
            .andExpect(jsonPath("$.data.version").value(3));
    }

    @Test
    void missingAndInvalidUsersHaveStableErrors() throws Exception {
        String token = administrator();
        mvc.perform(get("/api/v1/admin/users/999999999").header("Authorization", bearer(token)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.requestId").isNotEmpty());
        mvc.perform(get("/api/v1/admin/users/not-a-number").header("Authorization", bearer(token)))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/admin/users?page=bad").header("Authorization", bearer(token)))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginName\":\" \",\"displayName\":\"Staff\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("loginName", "a".repeat(129),
                    "displayName", "Staff"))))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void cannotDisableLastEffectiveSystemAdministrator() throws Exception {
        long userId = user("admin" + suffix());
        try {
            Long roleId = jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code = 'SYSTEM_ADMIN'", Long.class);
            jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", userId, roleId);
            mvc.perform(post("/api/v1/admin/users/{userId}/disable", userId)
                    .header("Authorization", bearer(administrator())).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"expectedVersion\":0}"))
                .andExpect(status().isConflict());
            assertThat(jdbc.queryForObject("SELECT enabled FROM sys_user WHERE id = ?", Boolean.class, userId)).isTrue();
        } finally {
            jdbc.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM sys_user WHERE id = ?", userId);
        }
    }

    @Test
    void resetPasswordRevokesSessionsAndRequiresChange() throws Exception {
        long userId = user("staff" + suffix());
        String oldToken = issue(userId, Set.of("menu:home"), false);
        var response = mvc.perform(post("/api/v1/admin/users/{userId}/password-reset", userId)
                .header("Authorization", bearer(administrator())))
            .andExpect(status().isOk()).andReturn().getResponse();
        String secret = json.readTree(response.getContentAsString()).at("/data/value").asText();
        assertThat(secret).hasSize(24);
        assertThat(response.getHeader("Cache-Control")).contains("no-store");
        assertThat(jdbc.queryForObject("SELECT must_change_password FROM sys_user WHERE id = ?", Boolean.class, userId)).isTrue();
        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(oldToken)))
            .andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_security_event WHERE event_type = 'PASSWORD_RESET' AND target_user_id = ?", Integer.class, userId)).isEqualTo(1);
        String login = jdbc.queryForObject("SELECT login_name FROM sys_user WHERE id = ?", String.class, userId);
        var nextLogin = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("loginName", login, "password", secret))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty()).andReturn().getResponse();
        String nextToken = json.readTree(nextLogin.getContentAsString()).at("/data/accessToken").asText();
        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(nextToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.mustChangePassword").value(true));
    }

    @Test
    void redisOutagePreventsPasswordResetCommit() throws Exception {
        long userId = user("staff" + suffix());
        String before = jdbc.queryForObject("SELECT password_hash FROM sys_user WHERE id = ?", String.class, userId);
        doThrow(new RedisConnectionFailureException("test outage")).when(sessionSpy).revokeAllForUser(userId);
        mvc.perform(post("/api/v1/admin/users/{userId}/password-reset", userId)
                .header("Authorization", bearer(administrator())))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.code").value("DEPENDENCY_UNAVAILABLE"));
        assertThat(jdbc.queryForObject("SELECT password_hash FROM sys_user WHERE id = ?", String.class, userId))
            .isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_security_event WHERE event_type = 'PASSWORD_RESET' AND target_user_id = ?", Integer.class, userId)).isZero();
    }

    @Test
    void listUsesBoundedStablePagination() throws Exception {
        String token = administrator();
        user("staff" + suffix());
        mvc.perform(get("/api/v1/admin/users?page=0&size=1").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items.length()").value(1))
            .andExpect(jsonPath("$.data.size").value(1));
        mvc.perform(get("/api/v1/admin/users?page=0&size=10000").header("Authorization", bearer(token)))
            .andExpect(status().isBadRequest());
    }

    private long user(String login) {
        jdbc.update("INSERT INTO sys_user (login_name, login_name_normalized, display_name, password_hash) VALUES (?, ?, 'Staff', 'test-hash')", login, login.toLowerCase());
        return jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name = ?", Long.class, login);
    }

    private void effectiveAdmin() {
        long userId = user("admin" + suffix());
        Long roleId = jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code = 'SYSTEM_ADMIN'", Long.class);
        jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", userId, roleId);
    }

    private String administrator() {
        return issue(998877L, Set.of("menu:iam:users", "action:iam:user.manage"), false);
    }

    private String issue(long userId, Set<String> permissions, boolean mustChangePassword) {
        var lease = sessions.create(new LoginSnapshot(userId, "admin", "Admin",
            Set.of("SYSTEM_ADMIN"), permissions, mustChangePassword));
        leases.add(lease.sessionId());
        return tokens.issue(userId, lease.sessionId());
    }

    private static String bearer(String token) { return "Bearer " + token; }
    private static String suffix() { return UUID.randomUUID().toString().replace("-", "").substring(0, 12); }
}
