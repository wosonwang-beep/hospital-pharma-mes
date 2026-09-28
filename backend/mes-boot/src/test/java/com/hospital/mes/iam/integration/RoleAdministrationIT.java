package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.jwt.AccessTokenCodec;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
@Transactional
class RoleAdministrationIT {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private SessionStore sessions;
    @Autowired private AccessTokenCodec tokens;
    private final List<String> leases = new ArrayList<>();

    @AfterEach void cleanup() { leases.forEach(sessions::revoke); }

    @Test
    void createsReadsRenamesAndTogglesRoleWithVersionChecks() throws Exception {
        String code = "TEST_" + suffix().toUpperCase();
        String token = administrator();
        var created = mvc.perform(post("/api/v1/admin/roles").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("roleCode", code, "displayName", "Initial"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleCode").value(code))
            .andReturn().getResponse();
        long id = json.readTree(created.getContentAsString()).at("/data/id").asLong();
        mvc.perform(get("/api/v1/admin/roles/{roleId}", id).header("Authorization", bearer(token)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.permissionCodes").isArray());
        mvc.perform(patch("/api/v1/admin/roles/{roleId}/name", id).header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Renamed\",\"expectedVersion\":0}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.displayName").value("Renamed"));
        mvc.perform(patch("/api/v1/admin/roles/{roleId}/name", id).header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Stale\",\"expectedVersion\":0}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/api/v1/admin/roles/{roleId}/disable", id).header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("{\"expectedVersion\":1}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.enabled").value(false));
        mvc.perform(post("/api/v1/admin/roles/{roleId}/enable", id).header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("{\"expectedVersion\":2}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.enabled").value(true));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_security_event WHERE target_role_id = ? AND event_type = 'ROLE_CREATED'", Integer.class, id))
            .isEqualTo(1);
    }

    @Test
    void catalogIsRegisteredReadOnlyAndListIsBounded() throws Exception {
        String token = administrator();
        mvc.perform(get("/api/v1/admin/permissions").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.permissionCode=='menu:iam:users')]").isNotEmpty())
            .andExpect(jsonPath("$.data[?(@.permissionCode=='action:iam:role.manage')]").isNotEmpty());
        mvc.perform(get("/api/v1/admin/roles?page=0&size=1").header("Authorization", bearer(token)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(1));
        mvc.perform(get("/api/v1/admin/roles?page=0&size=10000").header("Authorization", bearer(token)))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/admin/permissions").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void duplicateMissingAndInvalidRolesHaveStableErrors() throws Exception {
        String code = "TEST_" + suffix().toUpperCase();
        String token = administrator();
        String body = json.writeValueAsString(Map.of("roleCode", code, "displayName", "Test"));
        mvc.perform(post("/api/v1/admin/roles").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());
        mvc.perform(post("/api/v1/admin/roles").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict());
        mvc.perform(get("/api/v1/admin/roles/999999999").header("Authorization", bearer(token)))
            .andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/admin/roles").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("roleCode", "bad role", "displayName", "Test"))))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void cannotDisableLastSystemAdministratorRole() throws Exception {
        long roleId = jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code = 'SYSTEM_ADMIN'", Long.class);
        long version = jdbc.queryForObject("SELECT version FROM sys_role WHERE id = ?", Long.class, roleId);
        mvc.perform(post("/api/v1/admin/roles/{roleId}/disable", roleId)
                .header("Authorization", bearer(administrator())).contentType(MediaType.APPLICATION_JSON)
                .content("{\"expectedVersion\":" + version + "}"))
            .andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT enabled FROM sys_role WHERE id = ?", Boolean.class, roleId))
            .isTrue();
    }

    private String administrator() {
        var lease = sessions.create(new LoginSnapshot(998877L, "admin", "Admin",
            Set.of("SYSTEM_ADMIN"), Set.of("menu:iam:roles", "action:iam:role.manage"), false));
        leases.add(lease.sessionId());
        return tokens.issue(998877L, lease.sessionId());
    }

    private static String bearer(String token) { return "Bearer " + token; }
    private static String suffix() { return UUID.randomUUID().toString().replace("-", "").substring(0, 12); }
}
