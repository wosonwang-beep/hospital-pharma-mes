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
    void migrationCreatesFrozenIamTables() {
        List<String> tables = jdbc.queryForList("""
            SELECT table_name FROM information_schema.tables
             WHERE table_schema = DATABASE() AND table_name IN
             ('sys_user', 'sys_role', 'sys_permission', 'sys_user_role',
              'sys_role_permission', 'sys_security_event', 'sys_menu', 'sys_role_menu')
            """, String.class);
        assertThat(tables).containsExactlyInAnyOrder("sys_user", "sys_role", "sys_permission",
            "sys_user_role", "sys_role_permission", "sys_security_event", "sys_menu", "sys_role_menu");
    }

    @Test
    void identityTablesHaveUniqueCodesAndOptimisticVersions() {
        assertThat(columns("sys_user")).contains("login_name_normalized", "version", "password_hash",
            "failed_login_count", "locked_until", "must_change_password");
        assertThat(columns("sys_role")).contains("role_code", "version", "enabled");
        assertThat(columns("sys_permission")).contains("permission_code", "permission_type", "enabled", "version");
        assertThat(columns("sys_menu")).contains("org_id", "parent_id", "menu_code", "menu_name",
            "route_path", "sort_no", "status", "created_by", "created_at", "updated_by",
            "updated_at", "version_no");
        assertThat(uniqueColumns("sys_user")).contains("login_name_normalized");
        assertThat(uniqueColumns("sys_role")).contains("role_code");
        assertThat(uniqueColumns("sys_permission")).contains("permission_code");
        assertThat(uniquePairs("sys_menu")).contains(List.of("org_id", "menu_code"));
        assertThat(uniquePairs("sys_role_menu")).contains(List.of("role_id", "menu_id"));
    }

    @Test
    void currentAssignmentsAreUniqueAndHaveNoRevokeColumns() {
        assertThat(columns("sys_user_role")).doesNotContain("revoked_at", "revoked_by", "granted_at", "granted_by");
        assertThat(columns("sys_role_permission")).doesNotContain("revoked_at", "revoked_by", "granted_at", "granted_by");
        assertThat(uniquePairs("sys_user_role")).contains(List.of("user_id", "role_id"));
        assertThat(uniquePairs("sys_role_permission")).contains(List.of("role_id", "permission_id"));
        assertThat(columns("sys_security_event")).contains("event_type", "outcome", "actor_user_id",
            "target_user_id", "target_permission_id", "trace_id", "occurred_at");
    }

    @Test
    void systemAdministratorReceivesBothIamMenus() {
        List<String> codes = jdbc.queryForList("""
            SELECT p.permission_code FROM sys_permission p
            JOIN sys_role_permission rp ON rp.permission_id = p.id
            JOIN sys_role r ON r.id = rp.role_id
            WHERE r.role_code = 'SYSTEM_ADMIN'
            """, String.class);
        assertThat(codes).contains("menu:iam:users", "menu:iam:roles");
    }

    @Test
    void systemAdministratorReceivesFrozenIamPermissions() {
        List<String> codes = jdbc.queryForList("""
            SELECT p.permission_code FROM sys_permission p
            JOIN sys_role_permission rp ON rp.permission_id = p.id
            JOIN sys_role r ON r.id = rp.role_id
            WHERE r.role_code = 'SYSTEM_ADMIN'
            """, String.class);
        assertThat(codes).contains(
            "iam:user:view", "iam:user:create", "iam:user:update",
            "iam:role:view", "iam:role:create", "iam:role:update",
            "iam:permission:view", "iam:permission:create", "iam:permission:update",
            "iam:menu:view", "iam:menu:create", "iam:menu:update");
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

    private List<List<String>> uniquePairs(String table) {
        return jdbc.queryForList("""
            SELECT index_name FROM information_schema.statistics
             WHERE table_schema = DATABASE() AND table_name = ? AND non_unique = 0
             GROUP BY index_name HAVING COUNT(*) = 2
            """, String.class, table).stream().map(index -> jdbc.queryForList("""
                SELECT column_name FROM information_schema.statistics
                 WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?
                 ORDER BY seq_in_index
                """, String.class, table, index)).toList();
    }
}
