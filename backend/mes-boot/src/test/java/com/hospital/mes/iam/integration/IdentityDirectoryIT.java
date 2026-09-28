package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.identity.LoginIdentity;
import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.identity.SecurityEvent;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class IdentityDirectoryIT {

    @Autowired private IdentityDirectory identities;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void lookupUsesNormalizedLoginAndDatabaseRejectsDuplicate() {
        String login = "Staff" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        long id = user(login.toLowerCase(), true);
        LoginIdentity found = identities.findForLogin(login.toUpperCase()).orElseThrow();
        assertThat(found.userId()).isEqualTo(id);
        assertThat(found.enabled()).isTrue();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> user(login.toLowerCase(), true))
            .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void fifthFailureLocksForFifteenMinutesAndSuccessClearsCounter() {
        String login = "staff" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        long id = user(login, true);
        Instant now = Instant.parse("2026-09-28T00:00:00Z");
        for (int i = 0; i < 4; i++) identities.recordLoginFailure(id, now);
        assertThat(jdbc.queryForObject("SELECT failed_login_count FROM sys_user WHERE id = ?", Integer.class, id))
            .isEqualTo(4);
        assertThat(identities.findForLogin(login).orElseThrow().lockedUntil()).isNull();
        identities.recordLoginFailure(id, now);
        assertThat(identities.findForLogin(login).orElseThrow().lockedUntil())
            .isEqualTo(now.plus(15, ChronoUnit.MINUTES));
        identities.recordLoginSuccess(id, now.plus(16, ChronoUnit.MINUTES));
        assertThat(identities.findForLogin(login).orElseThrow().failedLoginCount()).isZero();
        assertThat(identities.findForLogin(login).orElseThrow().lockedUntil()).isNull();
    }

    @Test
    void expiredLockStartsAnewFailureSequence() {
        String login = "staff" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        long id = user(login, true);
        Instant now = Instant.parse("2026-09-28T00:00:00Z");
        for (int i = 0; i < 5; i++) identities.recordLoginFailure(id, now);
        identities.recordLoginFailure(id, now.plus(16, ChronoUnit.MINUTES));
        LoginIdentity identity = identities.findForLogin(login).orElseThrow();
        assertThat(identity.failedLoginCount()).isEqualTo(1);
        assertThat(identity.lockedUntil()).isNull();
    }

    @Test
    void snapshotUnionsEnabledRolesAndPermissionsButExcludesRevokedGrants() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        long id = user("staff" + suffix, true);
        long firstRole = role("R1_" + suffix, true);
        long secondRole = role("R2_" + suffix, true);
        long disabledRole = role("R3_" + suffix, false);
        long firstPermission = permission("action:test:first:" + suffix, true);
        long secondPermission = permission("action:test:second:" + suffix, true);
        long disabledPermission = permission("action:test:disabled:" + suffix, false);
        grantRole(id, firstRole, false);
        grantRole(id, secondRole, false);
        grantRole(id, disabledRole, false);
        grantPermission(firstRole, firstPermission, false);
        grantPermission(secondRole, secondPermission, false);
        grantPermission(secondRole, firstPermission, false);
        grantPermission(firstRole, disabledPermission, false);
        grantPermission(disabledRole, disabledPermission, false);
        grantPermission(firstRole, secondPermission, true);

        LoginSnapshot snapshot = identities.loadLoginSnapshot(id);
        assertThat(snapshot.roleCodes()).containsExactlyInAnyOrder("R1_" + suffix, "R2_" + suffix);
        assertThat(snapshot.permissionCodes()).containsExactlyInAnyOrder(
            "action:test:first:" + suffix, "action:test:second:" + suffix);
    }

    @Test
    void securityEventDoesNotPersistCredentialMaterial() {
        String trace = "trace-" + UUID.randomUUID();
        identities.appendSecurityEvent(new SecurityEvent("LOGIN", "DENIED", null, null, null,
            trace, "login failed", Instant.parse("2026-09-28T00:00:00Z")));
        var row = jdbc.queryForMap("SELECT * FROM sys_security_event WHERE trace_id = ?", trace);
        assertThat(row.get("event_type")).isEqualTo("LOGIN");
        assertThat(row.get("outcome")).isEqualTo("DENIED");
        assertThat(row.values().toString()).doesNotContain("password", "secret", "token");
    }

    private long user(String login, boolean enabled) {
        jdbc.update("""
            INSERT INTO sys_user (login_name, login_name_normalized, display_name, password_hash, enabled)
            VALUES (?, ?, 'Test Employee', 'test-hash', ?)
            """, login, login.toLowerCase(), enabled);
        return jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized = ?", Long.class,
            login.toLowerCase());
    }

    private long role(String code, boolean enabled) {
        jdbc.update("INSERT INTO sys_role (role_code, display_name, enabled) VALUES (?, ?, ?)", code, code, enabled);
        return jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code = ?", Long.class, code);
    }

    private long permission(String code, boolean enabled) {
        jdbc.update("""
            INSERT INTO sys_permission (permission_code, permission_type, display_name, enabled)
            VALUES (?, 'ACTION', ?, ?)
            """, code, code, enabled);
        return jdbc.queryForObject("SELECT id FROM sys_permission WHERE permission_code = ?", Long.class, code);
    }

    private void grantRole(long userId, long roleId, boolean revoked) {
        jdbc.update("INSERT INTO sys_user_role (user_id, role_id, revoked_at) VALUES (?, ?, ?)",
            userId, roleId, revoked ? java.sql.Timestamp.from(Instant.now()) : null);
    }

    private void grantPermission(long roleId, long permissionId, boolean revoked) {
        jdbc.update("INSERT INTO sys_role_permission (role_id, permission_id, revoked_at) VALUES (?, ?, ?)",
            roleId, permissionId, revoked ? java.sql.Timestamp.from(Instant.now()) : null);
    }
}
