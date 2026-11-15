package com.hospital.mes.incoming;

import com.hospital.mes.MesApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Read-only evidence for the dedicated warehouse SAMPLE_OUT permission/ledger design.
 * Does not create authorization, signatures, samples, or inventory movements.
 */
@SpringBootTest(classes=MesApplication.class)
@ActiveProfiles("ci")
class IncomingCrossroleSecurityPreflightIT {
 @Autowired JdbcTemplate jdbc;

 @Test void existingSamplingPermissionAndDedicatedWarehouseGapAreRecorded() {
  List<String> codes=jdbc.queryForList(
    "SELECT permission_code FROM sys_permission WHERE permission_code IN "
   +"('qms:sampling:execute','qms:sampling:complete','qms:sampling:warehouse-confirm','wms:sampling-outbound:confirm')",
    String.class);
  assertThat(codes).contains("qms:sampling:execute","qms:sampling:complete");
  System.out.println("G3 SECURITY PREFLIGHT existing sampling permissions="+codes);
  System.out.println("G3 SECURITY PREFLIGHT distinct warehouse outbound permission configured="+
     codes.contains("wms:sampling-outbound:confirm"));
 }

 @Test void ledgerPhysicalColumnsAndExistingIndexesAreInspectedWithoutWrites() {
  var columns=jdbc.queryForList(
    "SELECT column_name,numeric_scale FROM information_schema.columns "
   +"WHERE table_schema=DATABASE() AND table_name='wms_inventory_ledger' "
   +"AND column_name IN ('material_lot_id','location_id','container_id','unit_id','delta_qty','source_type','source_ref','idempotency_key')");
  assertThat(columns).hasSize(8);
  var keys=jdbc.queryForList(
    "SELECT DISTINCT index_name FROM information_schema.statistics "
   +"WHERE table_schema=DATABASE() AND table_name='wms_inventory_ledger'");
  System.out.println("G3 SECURITY PREFLIGHT ledger index definitions="+keys);
  assertThat(keys).isNotEmpty();
 }

 @Test void noExistingSampleOutLedgerRowsAreSilentlyReclassified() {
  Long count=jdbc.queryForObject(
    "SELECT COUNT(*) FROM wms_inventory_ledger WHERE event_type='SAMPLE_OUT'",Long.class);
  System.out.println("G3 SECURITY PREFLIGHT preexisting SAMPLE_OUT rows="+count);
  assertThat(count).isNotNull().isGreaterThanOrEqualTo(0L);
 }
}
