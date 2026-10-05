package com.hospital.mes.production;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Read-only schema gate; never changes shared records or migration history. */
@SpringBootTest
@ActiveProfiles("ci")
class ProductionQualitySchemaIT {
    @Autowired JdbcTemplate jdbc;
    @Test void additiveScopeConstraintsDoNotInvalidateHistoricalFacts() {
        org.junit.jupiter.api.Assumptions.assumeTrue(jdbc.queryForObject("SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success=1",Integer.class)<25,"One-time V025 preflight; do not assert that later production data is empty");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_sample WHERE sample_scope='PRODUCTION'", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_deviation WHERE investigation_scope='PRODUCTION' AND (main_batch_id IS NULL OR investigation_kind='OOS')", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.check_constraints WHERE constraint_schema=DATABASE() AND table_name='qms_deviation' AND check_clause LIKE '%investigation_kind%' AND check_clause LIKE '%test_execution_id%'", Integer.class)).isEqualTo(1);
    }
    @Test void productionQualityKeepsImmutableFactsAndDisjointResultOwnership() {
        for (String table : new String[]{"qms_production_plan", "mes_balance_rule", "mes_balance_result",
                "mes_balance_investigation", "qms_production_test_instance", "qms_production_test_review",
                "qms_capa", "qms_capa_review"}) {
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?", Integer.class, table))
                    .as(table).isEqualTo(1);
        }
        for (String table : new String[]{"mes_balance_rule", "mes_balance_result", "qms_production_test_review", "qms_capa_review"}) {
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema=DATABASE() AND event_object_table=? AND event_manipulation IN ('UPDATE','DELETE')", Integer.class, table))
                    .as(table + " append-only guards").isEqualTo(2);
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND constraint_name='ck_test_result_scope'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=0", Integer.class)).isZero();
    }
}
