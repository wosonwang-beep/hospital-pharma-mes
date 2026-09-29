package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.password.PasswordService;
import com.hospital.mes.security.session.SessionStore;
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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
@Transactional
class Mes002ContractIT {
    private static final Set<String> ALL_IAM = Set.of(
        "iam:user:view", "iam:user:create", "iam:user:update",
        "iam:role:view", "iam:role:create", "iam:role:update",
        "iam:permission:view", "iam:permission:create", "iam:permission:update",
        "iam:menu:view", "iam:menu:create", "iam:menu:update");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordService passwords;
    @Autowired SessionStore sessions;
    @Autowired AccessTokenCodec tokens;
    private final List<String> sessionIds = new ArrayList<>();

    @AfterEach void cleanup() { sessionIds.forEach(sessions::revoke); }

    @Test
    void tcIam001ActiveUserCanLogin() throws Exception {
        String password = "Valid passphrase 12345";
        String login = databaseUser(true, password);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(Map.of("loginName", login, "password", password))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void tcIam002DisabledUserCannotLogin() throws Exception {
        String password = "Valid passphrase 12345";
        String login = databaseUser(false, password);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(Map.of("loginName", login, "password", password))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void tcIam003UnauthorizedAccessIsDenied() throws Exception {
        String token = issue(Set.of("iam:user:view"));
        mvc.perform(get("/api/v1/users").header("Authorization", bearer(token)))
            .andExpect(status().isOk());
        mvc.perform(post("/api/v1/users").header("Authorization", bearer(token))
                .header("Idempotency-Key", "forbidden-create").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"forbidden\",\"displayName\":\"Forbidden\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/materials").header("Authorization", bearer(token)))
            .andExpect(status().isForbidden());
    }

    @Test
    void tcIam004PermissionAndNavigationAreConsistent() throws Exception {
        String token = issue(ALL_IAM);
        mvc.perform(get("/api/v1/menus").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[?(@.menuCode == 'iam:user:view')].routePath")
                .value(org.hamcrest.Matchers.hasItem("/admin/users")))
            .andExpect(jsonPath("$.data.items[?(@.menuCode == 'iam:role:view')].routePath")
                .value(org.hamcrest.Matchers.hasItem("/admin/roles")));
        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.permissionCodes", org.hamcrest.Matchers.hasItems(
                "iam:user:view", "iam:role:view")));
    }

    @Test
    void frozenUserApiIsIdempotentAuditedAndOptimisticallyLocked() throws Exception {
        String token = issue(ALL_IAM);
        String key = "create-user-" + suffix();
        String username = "formal" + suffix();
        byte[] body = json.writeValueAsBytes(Map.of("username", username, "displayName", "Formal User",
            "roleIds", List.of(), "reason", "approved provisioning"));
        String first = mvc.perform(post("/api/v1/users").header("Authorization", bearer(token))
                .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.user.id").isString())
            .andReturn().getResponse().getContentAsString();
        String replay = mvc.perform(post("/api/v1/users").header("Authorization", bearer(token))
                .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode firstData = json.readTree(first).path("data");
        JsonNode replayData = json.readTree(replay).path("data");
        assertThat(replayData).isEqualTo(firstData);
        String id = firstData.path("user").path("id").asText();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE login_name = ?", Integer.class, username))
            .isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE action = 'IAM_USER_CREATED' AND object_id = ?", Integer.class, id))
            .isEqualTo(1);

        mvc.perform(put("/api/v1/users/{id}", id).header("Authorization", bearer(token))
                .header("Idempotency-Key", "update-user-" + suffix()).header("If-Match", "\"0\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Updated User\",\"status\":\"ACTIVE\",\"reason\":\"approved\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.version").value(1));
        mvc.perform(put("/api/v1/users/{id}", id).header("Authorization", bearer(token))
                .header("Idempotency-Key", "stale-user-" + suffix()).header("If-Match", "\"0\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Stale\",\"status\":\"ACTIVE\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void runtimeOpenApiExposesFrozenOperationIdsAndHeaders() throws Exception {
        String document = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        JsonNode paths = json.readTree(document).path("paths");
        assertThat(paths.path("/api/v1/users").at("/get/operationId").asText()).isEqualTo("listUsers");
        assertThat(paths.path("/api/v1/users").at("/post/operationId").asText()).isEqualTo("createUsers");
        assertThat(paths.path("/api/v1/roles/{id}/permissions").at("/post/operationId").asText()).isEqualTo("assignRolePermissions");
        assertThat(paths.path("/api/v1/permissions/{id}").at("/put/operationId").asText()).isEqualTo("updatePermissions");
        assertThat(paths.path("/api/v1/menus/{id}").at("/put/operationId").asText()).isEqualTo("updateMenus");
        assertThat(paths.path("/api/v1/users/{id}").at("/put/parameters").toString()).contains("Idempotency-Key", "If-Match");
    }

    private String databaseUser(boolean enabled, String password) {
        String login = "login" + suffix();
        jdbc.update("""
            INSERT INTO sys_user (login_name, login_name_normalized, display_name, password_hash,
                                  enabled, must_change_password)
            VALUES (?, ?, 'Login Test', ?, ?, FALSE)
            """, login, login.toLowerCase(), passwords.hash(password), enabled);
        long id = jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name = ?", Long.class, login);
        long role = jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code = 'SYSTEM_ADMIN'", Long.class);
        jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", id, role);
        return login;
    }

    private String issue(Set<String> permissions) {
        String login = "actor" + suffix();
        jdbc.update("INSERT INTO sys_user (login_name, login_name_normalized, display_name, password_hash, must_change_password) VALUES (?, ?, 'API Actor', 'not-used', FALSE)",
            login, login.toLowerCase());
        long actorId = jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name = ?", Long.class, login);
        var lease = sessions.create(new LoginSnapshot(actorId, 1L, login, "Admin",
            Set.of("SYSTEM_ADMIN"), permissions, false));
        sessionIds.add(lease.sessionId());
        return tokens.issue(actorId, lease.sessionId());
    }

    private static String bearer(String token) { return "Bearer " + token; }
    private static String suffix() { return UUID.randomUUID().toString().replace("-", "").substring(0, 12); }
}
