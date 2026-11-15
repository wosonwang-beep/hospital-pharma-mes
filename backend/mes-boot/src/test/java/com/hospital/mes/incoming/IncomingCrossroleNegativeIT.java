package com.hospital.mes.incoming;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.audit.domain.AuditCommand;
import com.hospital.mes.common.exception.ComplianceException;
import com.hospital.mes.common.exception.ResourceConflictException;
import com.hospital.mes.masterdata.application.UnitConversionCommands;
import com.hospital.mes.masterdata.application.UnitConversionService;
import com.hospital.mes.masterdata.application.UnitCommands;
import com.hospital.mes.wms.domain.WmsCommands.LocationCreate;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Real MariaDB negative contracts. The inherited fixture and every method roll back. */
class IncomingCrossroleNegativeIT extends IncomingQualityFixture {
 @Autowired UnitConversionService conversions;

 @Test void anotherWarehouseActorCannotDeductTheSameSamplingRecordTwice(){
  var generated=createGenerated("0.125",unit);var pending=pending(generated,"0.125",unit,null);
  long firstWarehouse=warehouseActor("甲");long secondWarehouse=warehouseActor("乙");warehouse(firstWarehouse);
  var completed=service.samplingAction(id(pending),"confirm-sample-outbound",body("reason","确认样品实物出库","reauthToken","token"),token(pending),key());
  BigDecimal after=balance();long ledgerCount=outboundCount(id(pending));warehouse(secondWarehouse);

  assertThatThrownBy(()->nested(()->service.samplingAction(id(completed),"confirm-sample-outbound",body("reason","复核同一取样记录","reauthToken","token"),token(completed),key()))).isInstanceOf(RuntimeException.class);
  assertThat(balance()).isEqualByComparingTo(after);assertThat(outboundCount(id(pending))).isEqualTo(ledgerCount).isEqualTo(1);
 }

 @Test void signatureFailureRollsBackLedgerLinkTaskAndWarehouseEvidence(){
  var generated=createGenerated("0.125",unit);var pending=pending(generated,"0.125",unit,null);long warehouse=warehouseActor("丙");warehouse(warehouse);BigDecimal before=balance();long detail=idLong(pending.path("details").get(0));
  doThrow(new IllegalStateException("injected signature dependency failure")).when(reauth).consume(anyString(),any());

  assertThatThrownBy(()->nested(()->service.samplingAction(id(pending),"confirm-sample-outbound",body("reason","确认样品实物出库","reauthToken","token"),token(pending),key()))).hasMessageContaining("injected signature dependency failure");

  assertThat(balance()).isEqualByComparingTo(before);assertThat(outboundCount(id(pending))).isZero();assertThat(jdbc.queryForObject("SELECT sample_outbound_ledger_id FROM qms_sampling_detail WHERE id=?",Long.class,detail)).isNull();assertThat(jdbc.queryForObject("SELECT status FROM qms_sampling_task WHERE id=?",String.class,id(pending))).isEqualTo("PENDING_WAREHOUSE");assertThat(jdbc.queryForObject("SELECT warehouse_signature_id FROM qms_sampling_task WHERE id=?",Long.class,id(pending))).isNull();
 }

 @Test void auditFailureRollsBackTheWholeWarehouseConfirmation(){
  var generated=createGenerated("0.125",unit);var pending=pending(generated,"0.125",unit,null);long warehouse=warehouseActor("丁");warehouse(warehouse);BigDecimal before=balance();
  doAnswer(invocation->{var command=(AuditCommand)invocation.getArgument(0);if("InventoryLedger:SAMPLE_OUT".equals(command.action()))throw new IllegalStateException("injected outbound audit failure");return invocation.callRealMethod();}).when(audit).append(any());

  assertThatThrownBy(()->nested(()->service.samplingAction(id(pending),"confirm-sample-outbound",body("reason","确认样品实物出库","reauthToken","token"),token(pending),key()))).hasMessageContaining("injected outbound audit failure");

  assertThat(balance()).isEqualByComparingTo(before);assertThat(outboundCount(id(pending))).isZero();assertThat(jdbc.queryForObject("SELECT status FROM qms_sampling_task WHERE id=?",String.class,id(pending))).isEqualTo("PENDING_WAREHOUSE");assertThat(jdbc.queryForObject("SELECT warehouse_signature_id FROM qms_sampling_task WHERE id=?",Long.class,id(pending))).isNull();
 }

 @Test void insufficientExplicitLocationDoesNotBorrowStockFromAnotherLocation(){
  long warehouseId=jdbc.queryForObject("SELECT l.warehouse_id FROM wms_location l JOIN wms_material_receipt_item i ON i.location_id=l.id JOIN md_material_lot ml ON ml.receipt_item_id=i.id WHERE ml.id=?",Long.class,lot);
  String emptyLocation=wms.maintenance("Location",null,new LocationCreate(Long.toString(warehouseId),"ISO"+suffix,"隔离取样位"),null,key()).path("id").asText();
  var generated=createGenerated("0.125",unit);var pending=pending(generated,"0.125",unit,emptyLocation);long warehouse=warehouseActor("戊");warehouse(warehouse);BigDecimal before=balance();

  assertThatThrownBy(()->nested(()->service.samplingAction(id(pending),"confirm-sample-outbound",body("reason","核对指定库位库存","reauthToken","token"),token(pending),key())))
   .isInstanceOfSatisfying(ComplianceException.class,error->assertThat(error.code()).isEqualTo("INSUFFICIENT_INVENTORY"));
  assertThat(balance()).isEqualByComparingTo(before);assertThat(outboundCount(id(pending))).isZero();
 }

 @Test void approvedThousandGramsEqualsOneKilogramWithoutLossAndSubSixDecimalConversionIsRejected(){
  as(analyst);String gram=units.create(new UnitCommands.Create("UG"+suffix,"克","MASS",6),key()).path("id").asText();conversions.create(new UnitConversionCommands.Create(gram,unit,"0.001",material),key());
  var generated=createGenerated("1",unit);String samplingId=id(generated.path("samplingTask"));
  assertThatThrownBy(()->nested(()->{var excessive=pending(generated,"0.000001",gram,null);warehouse(warehouseActor("庚"));service.samplingAction(id(excessive),"confirm-sample-outbound",body("reason","拒绝账本精度损失","reauthToken","token"),token(excessive),key());}))
   .isInstanceOfSatisfying(ComplianceException.class,error->assertThat(error.code()).isEqualTo("QUANTITY_PRECISION_LOSS"));
  assertThat(outboundCount(samplingId)).isZero();assertThat(jdbc.queryForObject("SELECT status FROM qms_sampling_task WHERE id=?",String.class,samplingId)).isEqualTo("PLANNED");

  as(analyst);var exact=pending(generated,"1000",gram,null);long warehouse=warehouseActor("己");warehouse(warehouse);BigDecimal before=balance();service.samplingAction(id(exact),"confirm-sample-outbound",body("reason","确认克与千克批准换算","reauthToken","token"),token(exact),key());assertThat(balance().subtract(before)).isEqualByComparingTo("-1.000000");
 }

 @Test void unreceivedSampleCannotBindToWaitingInspectionTask(){
  var generated=createGenerated("0.125",unit);var pending=pending(generated,"0.125",unit,null);warehouse(warehouseActor("辛"));var completed=service.samplingAction(id(pending),"confirm-sample-outbound",body("reason","确认样品实物出库","reauthToken","token"),token(pending),key());var sample=completed.path("details").get(0).path("samples").get(0);var task=generated.path("inspectionTask");as(analyst);

  assertThatThrownBy(()->nested(()->service.bindSample(id(task),body("sampleId",id(sample),"reason","样品尚未接收不得绑定"),token(task),key())))
   .isInstanceOf(ResourceConflictException.class).hasMessageContaining("COLLECTED");
  assertThat(jdbc.queryForObject("SELECT sample_id FROM qms_inspection_task WHERE id=?",Long.class,id(task))).isNull();
 }

 @Test void failedReviewedResultCreatesOnlyFailedDraftAndNeverQaRelease(){
  var generated=createGenerated("0.125",unit);var pending=pending(generated,"0.125",unit,null);warehouse(warehouseActor("壬"));var completed=service.samplingAction(id(pending),"confirm-sample-outbound",body("reason","确认样品实物出库","reauthToken","token"),token(pending),key());var sample=completed.path("details").get(0).path("samples").get(0);as(analyst);sample=service.sampleAction(id(sample),"receive",body("receivedAt",Instant.now().toString(),"storageLocation","检验样品柜一层","reason","检验室接收"),token(sample),key());var task=generated.path("inspectionTask");task=service.bindSample(id(task),body("sampleId",id(sample),"reason","绑定已接收检验样"),token(task),key());task=service.taskAction(id(task),"assign",body("assignedTo",Long.toString(analyst),"reason","派发检验"),token(task),key());task=service.taskAction(id(task),"start",body("reason","开始检验"),token(task),key());var execution=execution(task);var result=result(task,execution,"4","FAIL");review(task,result);

  var report=jdbc.queryForMap("SELECT status,overall_result FROM qms_inspection_report WHERE auto_generated_request_id=?",id(generated.path("request")));
  assertThat(report).containsEntry("status","DRAFT").containsEntry("overall_result","FAIL");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_release_decision WHERE material_lot_id=?",Long.class,lot)).isZero();assertThat(jdbc.queryForObject("SELECT quality_status FROM md_material_lot WHERE id=?",String.class,lot)).isNotEqualTo("RELEASED");
 }

 private JsonNode createGenerated(String planned,String plannedUnit){var source=jdbc.queryForMap("SELECT CAST(ri.receipt_id AS CHAR) receipt_id,CAST(ri.id AS CHAR) item_id FROM wms_material_receipt_item ri JOIN md_material_lot ml ON ml.receipt_item_id=ri.id WHERE ml.id=?",lot);var request=service.createInitialRequestFromReceipt((String)source.get("receipt_id"),(String)source.get("item_id"),key());return service.confirmAndGenerate(id(request),body("qcSpecificationVersionId",version,"plannedSamplingQuantity",planned,"plannedSamplingUnitId",plannedUnit,"requiredPackageCount",1,"samplingPlan","按批准方案抽取一件","reason","确认首次请验计划"),token(request),key());}
 private JsonNode pending(JsonNode generated,String quantity,String detailUnit,String sourceLocation){var sampling=generated.path("samplingTask");as(analyst);sampling=service.samplingAction(id(sampling),"assign",body("assignedTo",Long.toString(analyst),"reason","指定取样员"),token(sampling),key());sampling=service.samplingAction(id(sampling),"start",body("reason","开始取样"),token(sampling),key());var command=body("containerNo","原辅料包装一","sampleQuantity",quantity,"unitId",detailUnit,"sampledAt",Instant.now().minusSeconds(2).toString(),"packageResealed",true,"samples",List.of(Map.of("sampleType","TEST_SAMPLE","quantity",quantity)),"reason","记录实际取样");if(sourceLocation!=null)command.put("sourceLocationId",sourceLocation);sampling=service.samplingAction(id(sampling),"details",command,token(sampling),key());return service.samplingAction(id(sampling),"submit-to-warehouse",body("reason","提交仓库核对","reauthToken","token"),token(sampling),key());}
 private long warehouseActor(String code){String login="iq_wh_"+code.codePointAt(0)+suffix;jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",login,login,"仓库复核员"+code,"local-fixture");long id=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,login);jdbc.update("INSERT INTO md_qualification(org_id,created_by,updated_by,user_id,qualification_code,valid_from,valid_to,status) VALUES(1,?,?,?,'IQ_INCOMING_IT',?,?,'ACTIVE')",id,id,id,LocalDate.now(ZoneOffset.UTC).minusDays(1),LocalDate.now(ZoneOffset.UTC).plusYears(1));return id;}
 private void warehouse(long actor){permissions.addAll(Set.of("qms:sampling:warehouse-confirm","wms:inventory:sample-outbound"));as(actor);}
 private void nested(Runnable action){var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);tx.execute(status->{action.run();return null;});}
 private BigDecimal balance(){return jdbc.queryForObject("SELECT COALESCE(SUM(delta_qty),0) FROM wms_inventory_ledger WHERE material_lot_id=?",BigDecimal.class,lot);}
 private long outboundCount(String samplingId){return jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger l JOIN qms_sampling_detail d ON l.source_ref=CONCAT('SamplingDetail/',d.id) WHERE d.sampling_task_id=? AND l.event_type='SAMPLE_OUT'",Long.class,samplingId);}
 private long idLong(JsonNode node){return Long.parseLong(id(node));}
}
