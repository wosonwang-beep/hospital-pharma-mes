package com.hospital.mes.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("ci")
class SystemAdminPermissionsIT {
    @Autowired JdbcTemplate jdbc;

    @Test
    void systemAdminHasEveryEnabledPermissionAndAdminUsesThatRole() {
        Integer enabled = jdbc.queryForObject(
            "SELECT COUNT(*) FROM sys_permission WHERE enabled=TRUE", Integer.class);
        Integer granted = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM sys_role r
            JOIN sys_role_permission rp ON rp.role_id=r.id
            JOIN sys_permission p ON p.id=rp.permission_id
            WHERE r.role_code='SYSTEM_ADMIN' AND r.enabled=TRUE AND p.enabled=TRUE
            """, Integer.class);
        Integer adminBinding = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM sys_user u
            JOIN sys_user_role ur ON ur.user_id=u.id
            JOIN sys_role r ON r.id=ur.role_id
            WHERE u.login_name_normalized='admin' AND r.role_code='SYSTEM_ADMIN' AND r.enabled=TRUE
            """, Integer.class);

        assertThat(granted).isEqualTo(enabled);
        assertThat(adminBinding).isEqualTo(1);

        List<String> required = List.of(
            "production:order:view",
            "production:batch:create",
            "production:batch:update",
            "mes:execution:view",
            "mes:operation:view",
            "mes:operation:start",
            "mes:weigh:create",
            "mes:weigh:verify",
            "mes:charge:create",
            "ebr:form:view",
            "ebr:form:edit",
            "ebr:form:submit",
            "trace:view"
        );
        List<String> actual = jdbc.queryForList(
            "SELECT permission_code FROM sys_permission WHERE enabled=TRUE", String.class);
        assertThat(actual).containsAll(required);
    }
}
