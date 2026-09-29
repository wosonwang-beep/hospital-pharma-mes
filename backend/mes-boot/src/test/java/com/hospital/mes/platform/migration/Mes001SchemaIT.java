package com.hospital.mes.platform.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("ci")
class Mes001SchemaIT {
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void createsExactlyTheFiveMes001PlatformTables() {
        List<String> tables = jdbc.queryForList("""
            SELECT table_name FROM information_schema.tables
             WHERE table_schema = DATABASE()
               AND table_name IN ('gxp_audit_event','gxp_signature','integration_inbox',
                                  'integration_outbox','platform_idempotency_record')
             ORDER BY table_name
            """, String.class);
        assertThat(tables).containsExactly("gxp_audit_event", "gxp_signature", "integration_inbox",
            "integration_outbox", "platform_idempotency_record");
    }

    @Test
    void installsAuditMutationGuardsAndFrozenPermissions() {
        Integer triggerCount = jdbc.queryForObject("""
            SELECT COUNT(*) FROM information_schema.triggers
             WHERE trigger_schema = DATABASE()
               AND event_object_table = 'gxp_audit_event'
               AND event_manipulation IN ('UPDATE','DELETE')
            """, Integer.class);
        assertThat(triggerCount).isEqualTo(2);

        List<String> permissions = jdbc.queryForList("""
            SELECT permission_code FROM sys_permission
             WHERE permission_code IN ('audit:view','ebr:sign','integration:view','integration:retry')
             ORDER BY permission_code
            """, String.class);
        assertThat(permissions).containsExactly("audit:view", "ebr:sign", "integration:retry", "integration:view");
    }
}
