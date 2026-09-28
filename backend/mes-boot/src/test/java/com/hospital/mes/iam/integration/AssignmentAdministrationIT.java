package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.session.SessionStore;
import com.hospital.mes.system.application.AdminSecurityEventWriter;
import com.hospital.mes.system.application.AssignmentAdministration;
import com.hospital.mes.common.exception.ResourceConflictException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
@Transactional
class AssignmentAdministrationIT {
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private SessionStore sessions;
    @Autowired private AccessTokenCodec tokens;
    @Autowired private AssignmentAdministration assignments;
    @MockitoSpyBean private AdminSecurityEventWriter eventSpy;
    private final List<String> leases = new ArrayList<>();
    @AfterEach void cleanup() {
        reset(eventSpy);
        leases.forEach(sessions::revoke);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void failedAssignmentEventRollsBackPair() throws Exception {
        long userId = user();
        long roleId = role();
        try {
            doThrow(new IllegalStateException("test event failure")).when(eventSpy)
                .append(eq("USER_ROLE_GRANTED"), any(), any(), any(), nullable(Long.class), any());
            mvc.perform(post("/api/v1/admin/users/{userId}/roles/{roleId}", userId, roleId)
                    .header("Authorization", bearer(administrator())))
                .andExpect(status().isInternalServerError());
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role WHERE user_id = ? AND role_id = ?",
                Integer.class, userId, roleId)).isZero();
        } finally {
            jdbc.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM sys_user WHERE id = ?", userId);
            jdbc.update("DELETE FROM sys_role WHERE id = ?", roleId);
        }
    }

    @Test
    void userRolePairIsCurrentStateAndEventsAreMinimal() throws Exception {
        effectiveAdmin();
        long userId = user();
        long roleId = role();
        String token = administrator();
        mvc.perform(post("/api/v1/admin/users/{userId}/roles/{roleId}", userId, roleId)
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role WHERE user_id = ? AND role_id = ?",
            Integer.class, userId, roleId)).isEqualTo(1);
        mvc.perform(post("/api/v1/admin/users/{userId}/roles/{roleId}", userId, roleId)
                .header("Authorization", bearer(token))).andExpect(status().isConflict());
        mvc.perform(delete("/api/v1/admin/users/{userId}/roles/{roleId}", userId, roleId)
                .header("Authorization", bearer(token))).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role WHERE user_id = ? AND role_id = ?",
            Integer.class, userId, roleId)).isZero();
        mvc.perform(delete("/api/v1/admin/users/{userId}/roles/{roleId}", userId, roleId)
                .header("Authorization", bearer(token))).andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_security_event WHERE event_type = 'USER_ROLE_GRANTED' AND actor_user_id = 998877 AND target_user_id = ? AND target_role_id = ?", Integer.class, userId, roleId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_security_event WHERE event_type = 'USER_ROLE_REVOKED' AND target_user_id = ? AND target_role_id = ?", Integer.class, userId, roleId)).isEqualTo(1);
    }

    @Test
    void rolePermissionPairRequiresRegisteredCode() throws Exception {
        effectiveAdmin();
        long roleId = role();
        String token = administrator();
        String code = "menu:iam:users";
        mvc.perform(post("/api/v1/admin/roles/{roleId}/permissions/{code}", roleId, code)
                .header("Authorization", bearer(token))).andExpect(status().isOk());
        mvc.perform(post("/api/v1/admin/roles/{roleId}/permissions/{code}", roleId, code)
                .header("Authorization", bearer(token))).andExpect(status().isConflict());
        mvc.perform(delete("/api/v1/admin/roles/{roleId}/permissions/{code}", roleId, code)
                .header("Authorization", bearer(token))).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/admin/roles/{roleId}/permissions/{code}", roleId, code)
                .header("Authorization", bearer(token))).andExpect(status().isConflict());
        mvc.perform(post("/api/v1/admin/roles/{roleId}/permissions/{code}", roleId, "action:unregistered:foo")
                .header("Authorization", bearer(token))).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_role_permission WHERE role_id = ?", Integer.class, roleId)).isZero();
        Long permissionId = jdbc.queryForObject("SELECT id FROM sys_permission WHERE permission_code = ?", Long.class, code);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_security_event WHERE event_type = 'ROLE_PERMISSION_GRANTED' AND target_role_id = ? AND target_permission_id = ?", Integer.class, roleId, permissionId)).isEqualTo(1);
    }

    @Test
    void missingTargetsReturnNotFound() throws Exception {
        String token = administrator();
        mvc.perform(post("/api/v1/admin/users/999999999/roles/999999999")
            .header("Authorization", bearer(token))).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/admin/roles/999999999/permissions/menu:iam:users")
            .header("Authorization", bearer(token))).andExpect(status().isNotFound());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void cannotRevokeOnlyEffectiveAdministratorAssignment() throws Exception {
        long userId = user();
        long roleId = jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code = 'SYSTEM_ADMIN'", Long.class);
        try {
            jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", userId, roleId);
            mvc.perform(delete("/api/v1/admin/users/{userId}/roles/{roleId}", userId, roleId)
                    .header("Authorization", bearer(administrator())))
                .andExpect(status().isConflict());
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role WHERE user_id = ? AND role_id = ?",
                Integer.class, userId, roleId)).isEqualTo(1);
        } finally {
            jdbc.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM sys_user WHERE id = ?", userId);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentRevokesLeaveOneEffectiveAdministrator() throws Exception {
        long first = user();
        long second = user();
        long roleId = jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code = 'SYSTEM_ADMIN'", Long.class);
        var pool = Executors.newFixedThreadPool(2);
        try {
            jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", first, roleId);
            jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", second, roleId);
            CountDownLatch start = new CountDownLatch(1);
            var one = pool.submit(() -> revokeAfterStart(start, first, roleId));
            var two = pool.submit(() -> revokeAfterStart(start, second, roleId));
            start.countDown();
            assertThat(List.of(one.get(10, TimeUnit.SECONDS), two.get(10, TimeUnit.SECONDS)))
                .containsExactlyInAnyOrder("OK", "CONFLICT");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role WHERE role_id = ? AND user_id IN (?, ?)",
                Integer.class, roleId, first, second)).isEqualTo(1);
        } finally {
            pool.shutdownNow();
            jdbc.update("DELETE FROM sys_security_event WHERE trace_id = 'concurrent-test' AND target_user_id IN (?, ?)", first, second);
            jdbc.update("DELETE FROM sys_user_role WHERE user_id IN (?, ?)", first, second);
            jdbc.update("DELETE FROM sys_user WHERE id IN (?, ?)", first, second);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void ordinaryRoleCarryingIamPermissionCannotBeRemovedFromLastAdministrator() throws Exception {
        long userId = user();
        long secondaryRoleId = role();
        long adminRoleId = jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code = 'SYSTEM_ADMIN'", Long.class);
        long permissionId = jdbc.queryForObject("SELECT id FROM sys_permission WHERE permission_code = 'menu:iam:roles'", Long.class);
        try {
            jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", userId, adminRoleId);
            jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", userId, secondaryRoleId);
            jdbc.update("INSERT INTO sys_role_permission (role_id, permission_id) VALUES (?, ?)", secondaryRoleId, permissionId);
            jdbc.update("DELETE FROM sys_role_permission WHERE role_id = ? AND permission_id = ?", adminRoleId, permissionId);
            String token = administrator();
            mvc.perform(delete("/api/v1/admin/users/{userId}/roles/{roleId}", userId, secondaryRoleId)
                    .header("Authorization", bearer(token)))
                .andExpect(status().isConflict());
            mvc.perform(post("/api/v1/admin/roles/{roleId}/disable", secondaryRoleId)
                    .header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"expectedVersion\":0}"))
                .andExpect(status().isConflict());
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role WHERE user_id = ? AND role_id = ?",
                Integer.class, userId, secondaryRoleId)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT enabled FROM sys_role WHERE id = ?", Boolean.class,
                secondaryRoleId)).isTrue();
        } finally {
            jdbc.update("INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES (?, ?)", adminRoleId, permissionId);
            jdbc.update("UPDATE sys_role SET enabled = TRUE WHERE id = ?", secondaryRoleId);
            jdbc.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM sys_role_permission WHERE role_id = ?", secondaryRoleId);
            jdbc.update("DELETE FROM sys_user WHERE id = ?", userId);
            jdbc.update("DELETE FROM sys_role WHERE id = ?", secondaryRoleId);
        }
    }

    private String revokeAfterStart(CountDownLatch start, long userId, long roleId) throws InterruptedException {
        start.await();
        try {
            assignments.revokeUserRole(userId, roleId, 998877L, "concurrent-test");
            return "OK";
        } catch (ResourceConflictException ex) {
            return "CONFLICT";
        }
    }

    private long user() {
        String login = "staff" + suffix();
        jdbc.update("INSERT INTO sys_user (login_name, login_name_normalized, display_name, password_hash) VALUES (?, ?, 'Staff', 'hash')", login, login);
        return jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name = ?", Long.class, login);
    }
    private void effectiveAdmin() {
        long userId = user();
        long roleId = jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code = 'SYSTEM_ADMIN'", Long.class);
        jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", userId, roleId);
    }
    private long role() {
        String code = "TEST_" + suffix().toUpperCase();
        jdbc.update("INSERT INTO sys_role (role_code, display_name) VALUES (?, 'Test')", code);
        return jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code = ?", Long.class, code);
    }
    private String administrator() {
        var lease = sessions.create(new LoginSnapshot(998877L, "admin", "Admin", Set.of("SYSTEM_ADMIN"),
            Set.of("menu:iam:users", "action:iam:user.manage", "menu:iam:roles", "action:iam:role.manage"), false));
        leases.add(lease.sessionId());
        return tokens.issue(998877L, lease.sessionId());
    }
    private static String bearer(String token) { return "Bearer " + token; }
    private static String suffix() { return UUID.randomUUID().toString().replace("-", "").substring(0, 12); }
}
