package com.hospital.mes.production;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.*;
/** Read-only prerequisites and schema checks; never creates regulated fixtures. */
@SpringBootTest @ActiveProfiles("ci")
class FinishedArchiveSchemaIT {
 @Autowired JdbcTemplate jdbc;
 @Test void finishedScopeIsCompatibleWithTheApprovedTargetConstraint(){
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_release_decision WHERE release_scope='FINISHED_PRODUCT' AND (finished_lot_id IS NULL OR inspection_report_id IS NOT NULL OR decision_source<>'USER_QA' OR decision NOT IN ('RELEASED','REJECTED') OR release_basis<>'FULL_INSPECTION')",Long.class)).isZero();
 }
 @Test void manifestHasScopedImmutableFileAndDecisionReferences(){
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='ebr_pdf_manifest'",Long.class)).isEqualTo(1);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.referential_constraints WHERE constraint_schema=DATABASE() AND table_name='ebr_pdf_manifest'",Long.class)).isGreaterThanOrEqualTo(5);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema=DATABASE() AND event_object_table='ebr_pdf_manifest'",Long.class)).isEqualTo(2);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=0",Long.class)).isZero();
 }
}
