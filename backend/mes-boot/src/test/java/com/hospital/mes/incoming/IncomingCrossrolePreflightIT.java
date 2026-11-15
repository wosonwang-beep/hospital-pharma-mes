package com.hospital.mes.incoming;

import com.hospital.mes.MesApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Read-only G3 preflight before designing an append-only cross-role migration.
 * This test intentionally performs no database mutation or auto-approval.
 */
@SpringBootTest(classes=MesApplication.class)
@ActiveProfiles("ci")
class IncomingCrossrolePreflightIT {
 @Autowired JdbcTemplate jdbc;

 @Test void firstInspectionHistoricalIdentityMustBeReviewedBeforeUniqueIndex() {
  List<Map<String,Object>> duplicates=jdbc.queryForList(
    "SELECT org_id,material_lot_id,COUNT(*) AS request_count FROM qms_inspection_request "
   +"WHERE request_type='INITIAL' GROUP BY org_id,material_lot_id HAVING COUNT(*)>1");
  System.out.println("G3 PREFLIGHT existing INITIAL duplicates="+duplicates.size());
  assertThat(duplicates).as("Historical duplicate FIRST-INSPECTION requests require explicit migration review").isEmpty();
 }

 @Test void sourceColumnsAndPrecisionMustBePhysicallyVerified() {
  List<Map<String,Object>> cols=jdbc.queryForList(
    "SELECT table_name,column_name,is_nullable,numeric_scale FROM information_schema.columns "
   +"WHERE table_schema=DATABASE() AND ((table_name='qms_inspection_request' AND column_name IN ('qc_specification_version_id','specification_content_hash'))"
   +" OR (table_name='qms_inspection_task' AND column_name='sample_id')"
   +" OR (table_name='wms_inventory_ledger' AND column_name IN ('material_lot_id','location_id','container_id','unit_id','delta_qty')))");
  System.out.println("G3 PREFLIGHT physical source columns="+cols);
  assertThat(cols).as("G3 required existing source columns").hasSize(8);
  var scales=cols.stream().filter(c->"wms_inventory_ledger".equals(c.get("table_name"))&&"delta_qty".equals(c.get("column_name"))).toList();
  assertThat(scales).hasSize(1);
  assertThat(((Number)scales.getFirst().get("numeric_scale")).intValue()).as("WMS ledger precision must be explicitly mapped to conversion").isGreaterThanOrEqualTo(6);
 }

 @Test void newPendingSamplelessStateMustNotExistBeforeApproval() {
  Long without=jdbc.queryForObject("SELECT COUNT(*) FROM qms_inspection_task WHERE sample_id IS NULL AND status<>'CREATED'",Long.class);
  System.out.println("G3 PREFLIGHT historical sampleless invalid tasks="+without);
  assertThat(without).isZero();
 }
}
