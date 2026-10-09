package com.hospital.mes.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.execution.application.ExecutionService;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
    "mes.qualification.required-codes.operation-start=MES_DEMO_GMP",
    "mes.qualification.required-codes.operation-resume=MES_DEMO_GMP"
})
@ActiveProfiles("ci")
class CorrectRunningOperationOwnerIT {
    @Autowired ExecutionService execution;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @MockitoBean CurrentPlatformContextResolver contexts;

    @Test
    void runningOperationIsHandedBackToProductionOperatorThroughStateMachine() {
        long actor = jdbc.queryForObject(
            "SELECT id FROM sys_user WHERE login_name_normalized='demo.production'", Long.class);
        var permissions = new HashSet<>(jdbc.queryForList(
            "SELECT permission_code FROM sys_permission WHERE enabled=TRUE", String.class));
        when(contexts.current()).thenReturn(new CurrentPlatformContext(
            1, actor, Set.of("PRODUCTION"), permissions,
            "production-owner-correction", "production-owner-correction"));

        String unitId = jdbc.queryForObject("""
            SELECT eu.id
            FROM prd_execution_unit eu
            JOIN prd_main_batch b ON b.id=eu.main_batch_id
            WHERE b.org_id=1 AND b.batch_no='20261007-KCL-002'
            ORDER BY eu.id DESC LIMIT 1
            """, String.class);

        JsonNode current = execution.operations(unitId).stream()
            .filter(row -> "IN_PROGRESS".equals(row.path("status").asText()))
            .findFirst().orElseThrow();
        String operationId = current.path("id").asText();

        JsonNode paused = execution.pause(
            operationId,
            body(current.path("versionNo").asLong(), "纠正集成测试岗位归属：生产工序暂停并交回生产岗位"),
            quote(current.path("versionNo").asLong()),
            UUID.randomUUID().toString());

        JsonNode resumed = execution.resume(
            operationId,
            body(paused.path("versionNo").asLong(), "生产岗位复核后恢复当前配制工序"),
            quote(paused.path("versionNo").asLong()),
            UUID.randomUUID().toString());

        assertThat(resumed.path("status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(resumed.path("operatorId").asLong()).isEqualTo(actor);
    }

    private JsonNode body(long version, String reason) {
        var node = json.createObjectNode();
        node.put("versionNo", version);
        node.put("reason", reason);
        return node;
    }

    private String quote(long version) { return "\"" + version + "\""; }
}
