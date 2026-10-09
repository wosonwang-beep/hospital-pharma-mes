package com.hospital.mes.production;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.execution.application.ProductionQuantityService;
import com.hospital.mes.wms.domain.WmsCommands.*;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.*;

class ProductionQuantityIT extends ProductionQualityPlanIT {
 @Autowired ProductionQuantityService quantities;
 @BeforeEach void quantityPermissions(){permissions.addAll(java.util.Set.of("balance:view","mes:quantity:record","mes:quantity:reverse"));as(author);}
 JsonNode approvedBatch(){var batch=preparedBatch(false);var p=plans.create(planBody(batch),key());as(qa);plans.approve(id(p),body("versionNo",0,"reason","Independent plan approval","signature",Map.of("reauthToken","token")),token(p),key());as(author);return startActualBatch(production.releaseBatch(id(batch),body("processPackageId",packageVersionId,"ebrTemplateVersionId",templateVersionId,"reason","Approved production"),token(batch),key()));}
 JsonNode output(JsonNode batch,String source,String amount){var wh=wms.maintenance("Warehouse",null,new WarehouseCreate("FW"+suffix,"Finished output","FINISHED"),null,key());var location=wms.maintenance("Location",null,new LocationCreate(id(wh),"FL"+suffix,"Finished output area"),null,key());return body("versionNo",batch.path("versionNo").asLong(),"eventType","OUTPUT","amount",amount,"unitId",unit,"sourceRef",source,"lotNo","FIN"+suffix,"locationId",id(location),"productionDate",java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString(),"expiryDate",java.time.LocalDate.now(java.time.ZoneOffset.UTC).plusYears(1).toString(),"reason","Measured actual output","signature",Map.of("reauthToken","token"));}
 @Test void outputDefersStockUntilWarehouseReceiptAndPreservesSignedReversal(){
  var batch=approvedBatch();var command=output(batch,"OUTPUT-"+suffix,"9");String recordKey=key();var fact=quantities.record(id(batch),command,token(batch),recordKey);
  assertThat(quantities.record(id(batch),command,token(batch),recordKey)).isEqualTo(fact);
  String lot=fact.path("materialLotId").asText();assertThat(stockQuery.materialLot(1,Long.parseLong(lot)).path("qualityStatus").asText()).isEqualTo("QUARANTINE");
  assertThat(production.batch(id(batch)).path("finishedLotId").asText()).isEqualTo(lot);
  assertThat(jdbc.queryForObject("SELECT COALESCE(SUM(delta_qty),0) FROM wms_inventory_ledger WHERE material_lot_id=?",java.math.BigDecimal.class,lot)).isEqualByComparingTo("0");
  var current=production.batch(id(batch));var reverse=quantities.reverse(id(fact),body("versionNo",current.path("versionNo").asLong(),"reason","Correct measured output by append-only reversal","signature",Map.of("reauthToken","token")),token(current),key());
  assertThat(reverse.path("reversalOfId").asText()).isEqualTo(id(fact));
  assertThat(jdbc.queryForObject("SELECT amount FROM mes_quantity_event WHERE id=?",java.math.BigDecimal.class,id(fact))).isEqualByComparingTo("9");
  assertThat(jdbc.queryForObject("SELECT COALESCE(SUM(delta_qty),0) FROM wms_inventory_ledger WHERE material_lot_id=?",java.math.BigDecimal.class,lot)).isEqualByComparingTo("0");
  assertThat(verifier.verify(1,fact.path("signatureId").asLong())).isTrue();
  current=production.batch(id(batch));var finalCurrent=current;
  blockedCode("QUANTITY_ALREADY_REVERSED",()->quantities.reverse(id(fact),body("versionNo",finalCurrent.path("versionNo").asLong(),"reason","No double reversal","signature",Map.of("reauthToken","token")),token(finalCurrent),key()));
 }
 @Test void clientCannotSupplyChargeAndStaleBatchVersionCreatesNoQuantityFact(){
  var batch=approvedBatch();var body=body("versionNo",batch.path("versionNo").asLong(),"eventType","CHARGE","amount","1","unitId",unit,"sourceRef","FAKE-"+suffix,"reason","Source owner must create charge","signature",Map.of("reauthToken","token"));
  rejected(IllegalArgumentException.class,()->quantities.record(id(batch),body,token(batch),key()));
  ((com.fasterxml.jackson.databind.node.ObjectNode)body).put("eventType","LOSS").put("amount","0");quantities.record(id(batch),body,token(batch),key());
  var changed=body.deepCopy();((com.fasterxml.jackson.databind.node.ObjectNode)changed).put("sourceRef","STALE-"+suffix);
  rejected(com.hospital.mes.common.exception.ResourceConflictException.class,()->quantities.record(id(batch),changed,token(batch),key()));
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_quantity_event WHERE main_batch_id=?",Integer.class,id(batch))).isEqualTo(1);
 }
 @Test void auditFailureRollsBackOutputAndRootThenReplayCreatesOneFact(){
  var batch=approvedBatch();var command=output(batch,"ROLLBACK-"+suffix,"9");String commandKey=key();
  org.mockito.Mockito.doAnswer(inv->{var entry=(com.hospital.mes.audit.domain.AuditCommand)inv.getArgument(0);if(entry.action().equals("PRODUCTION_QUANTITY"))throw new IllegalStateException("Injected output audit failure");return inv.callRealMethod();}).when(audit).append(org.mockito.ArgumentMatchers.any());
  rejected(IllegalStateException.class,()->quantities.record(id(batch),command,token(batch),commandKey));
  assertThat(production.batch(id(batch)).path("finishedLotId").isNull()).isTrue();assertThat(production.batch(id(batch)).path("versionNo").asLong()).isEqualTo(batch.path("versionNo").asLong());
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM md_material_lot WHERE lot_no=?",Long.class,"FIN"+suffix)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_quantity_event WHERE main_batch_id=?",Long.class,id(batch))).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,commandKey)).isZero();org.mockito.Mockito.reset(audit);
  var first=quantities.record(id(batch),command,token(batch),commandKey);assertThat(quantities.record(id(batch),command,token(batch),commandKey)).isEqualTo(first);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger WHERE material_lot_id=?",Long.class,first.path("materialLotId").asText())).isZero();
 }
}
