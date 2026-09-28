package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("ci")
class IamSchemaIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void migrationCreatesSixIamTables() {
        List<String> tables = jdbc.queryForList("""
            SELECT table_name FROM information_schema.tables
             WHERE table_schema = DATABASE() AND table_name IN
             ('sys_user', 'sys_role', 'sys_permission', 'sys_user_role',
              'sys_role_permission', 'sys_security_event')
            """, String.class);
        assertThat(tables).containsExactlyInAnyOrder("sys_user", "sys_role", "sys_permission",
            "sys_user_role", "sys_role_permission", "sys_security_event");
    }

    @Test
    void identityTablesHaveUniqueCodesAndOptimisticVersions() {
        assertThat(columns("sys_user")).contains("login_name_normalized", "version", "password_hash",
            "failed_login_count", "locked_until", "must_change_password");
        assertThat(columns("sys_role")).contains("role_code", "version", "enabled");
        assertThat(columns("sys_permission")).contains("permission_code", "permission_type", "enabled");
        assertThat(uniqueColumns("sys_user")).contains("login_name_normalized");
        assertThat(uniqueColumns("sys_role")).contains("role_code");
        assertThat(uniqueColumns("sys_permission")).contains("permission_code");
    }

    @Test
    void assignmentsPreserveGrantAndRevokeMetadata() {
        assertThat(columns("sys_user_role")).contains("granted_at", "granted_by", "revoked_at", "revoked_by");
        assertThat(columns("sys_role_permission")).contains("granted_at", "granted_by", "revoked_at", "revoked_by");
        assertThat(columns("sys_security_event")).contains("event_type", "outcome", "actor_user_id",
            "target_user_id", "trace_id", "occurred_at");
    }

    @Test
    void migrationNeverSeedsAnAccount() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class);
        assertThat(count).isZero();
    }

    private List<String> columns(String table) {
        return jdbc.queryForList("""
            SELECT column_name FROM information_schema.columns
             WHERE table_schema = DATABASE() AND table_name = ?
            """, String.class, table);
    }

    private List<String> uniqueColumns(String table) {
        return jdbc.queryForList("""
            SELECT column_name FROM information_schema.statistics
             WHERE table_schema = DATABASE() AND table_name = ? AND non_unique = 0
            """, String.class, table);
    }
}
