package com.hospital.mes.iam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.security.identity.LoginSnapshot;
import com.hospital.mes.security.jwt.AccessTokenCodec;
import com.hospital.mes.security.session.SessionStore;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Test-only capability personas, real JWT/Redis/HTTP authorization, rollback-protected native DB (GET gate reads may synchronize quantity evidence).
 * Does not create or prescribe deployed role mappings. Domain command/gate tests remain separate. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
@org.springframework.transaction.annotation.Transactional
class FinalSystemPermissionIT {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired SessionStore sessions;
    @Autowired AccessTokenCodec tokens;
    @Autowired ObjectMapper json;

    static Stream<Arguments> personas() {
        return Stream.of(
            Arguments.of("SYSTEM_ADMIN", "", "/admin/users"),
            Arguments.of("PRODUCTION_OPERATOR", "production:batch:view", "/main-batches"),
            Arguments.of("PRODUCTION_REVIEWER", "qms:plan:view", "/quality/production-plans"),
            Arguments.of("WAREHOUSE", "wms:receipt:view", "/wms/receipts"),
            Arguments.of("WEIGHING", "mes:weigh:view", "/execution-units/%s/weighings"),
            Arguments.of("QC", "qms:specification:view", "/quality/specifications"),
            Arguments.of("QA", "qa:batch-review", "/qa/batches/%s/review-model"),
            Arguments.of("EQUIPMENT_ADMIN", "master:equipment:view", "/equipment"),
            Arguments.of("READ_ONLY", "production:batch:view", "/main-batches")
        );
    }

    @ParameterizedTest(name = "{0}: granted read, JWT snapshot, forbidden write and unknown route")
    @MethodSource("personas")
    void representativeCapabilitiesUseRealBackendAuthorization(String role, String permission, String path)
            throws Exception {
        Set<String> permissions;
        if (role.equals("SYSTEM_ADMIN")) {
            permissions = Set.copyOf(jdbc.queryForList("""
                SELECT p.permission_code FROM sys_role r
                JOIN sys_role_permission rp ON rp.role_id=r.id
                JOIN sys_permission p ON p.id=rp.permission_id
                WHERE r.role_code='SYSTEM_ADMIN' AND r.enabled=TRUE
                """, String.class));
        } else {
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_permission WHERE permission_code=?",
                Integer.class, permission)).isEqualTo(1);
            permissions = Set.of(permission);
        }
        if (path.contains("%s")) {
            String table = role.equals("QA") ? "prd_main_batch" : "prd_execution_unit";
            Long id = jdbc.queryForObject("SELECT MIN(id) FROM " + table + " WHERE org_id=1" + (role.equals("QA") ? " AND process_snapshot_id IS NOT NULL" : ""), Long.class);
            assertThat(id).as("Existing final-regression fixture for read-only role probe").isNotNull();
            path = path.formatted(id);
        }
        var lease = sessions.create(new LoginSnapshot(900000L, 1L, "final-permission-probe", role,
            Set.of(role), permissions, false));
        try {
            String authorization = "Bearer " + tokens.issue(900000L, lease.sessionId());
            var me = mvc.perform(get("/api/v1/auth/me").header("Authorization", authorization))
                .andExpect(status().isOk()).andReturn();
            var identity = json.readTree(me.getResponse().getContentAsString()).path("data");
            assertThat(identity.path("roleCodes").toString()).contains(role);
            assertThat(identity.path("permissionCodes").size()).isEqualTo(permissions.size());
            var read = mvc.perform(get("/api/v1" + path).header("Authorization", authorization))
                .andExpect(status().isOk()).andReturn();
            if (role.equals("QA")) {
                var model = json.readTree(read.getResponse().getContentAsString()).path("data");
                assertThat(model.path("gates").size()).isEqualTo(8);
                assertThat(java.util.stream.StreamSupport.stream(model.path("gates").spliterator(), false)
                    .map(gate -> gate.path("code").asText()).toList()).containsExactly(
                        "BATCH_QA_STATE", "QUALITY_PLAN", "EBR_REVIEW", "PRODUCTION_QC",
                        "MATERIAL_BALANCE", "FINISHED_INVENTORY", "FINISHED_RECEIPT", "FINISHED_INSPECTION_REPORT");
                assertThat(model.path("blockingCodes").size()).isGreaterThan(0);
                assertThat(model.path("allowedActions").size()).isZero();
            }
            mvc.perform(get("/api/v1/admin/unregistered-final-probe").header("Authorization", authorization))
                .andExpect(status().isForbidden());
            if (!role.equals("SYSTEM_ADMIN")) {
                mvc.perform(post("/api/v1/admin/users").header("Authorization", authorization)
                        .contentType("application/json").content("{}"))
                    .andExpect(status().isForbidden());
            }
        } finally {
            sessions.revoke(lease.sessionId());
        }
    }
}
