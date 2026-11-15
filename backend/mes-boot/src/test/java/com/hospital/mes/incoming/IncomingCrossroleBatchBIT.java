package com.hospital.mes.incoming;

import static org.assertj.core.api.Assertions.*;
import com.hospital.mes.masterdata.application.UnitCommands;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;

/** Real MariaDB transaction tests roll back with the fixture. */
class IncomingCrossroleBatchBIT extends IncomingQualityFixture {
 @Test void submitDoesNotMoveStockAndIndependentWarehouseConfirmationMovesOnceThenAllowsReceiveAndBind(){
  var request=createGenerated();var sampling=request.path("samplingTask");BigDecimal before=balance();
  sampling=service.samplingAction(id(sampling),"assign",body("assignedTo",Long.toString(analyst),"reason","指定取样员"),token(sampling),key());
  sampling=service.samplingAction(id(sampling),"start",body("reason","开始取样"),token(sampling),key());
  sampling=service.samplingAction(id(sampling),"details",body("containerNo","原辅料包装一","sampleQuantity","0.125","unitId",unit,"sampledAt",Instant.now().minusSeconds(2).toString(),"packageResealed",true,"samples",List.of(Map.of("sampleType","TEST_SAMPLE","quantity","0.100"),Map.of("sampleType","RETENTION_SAMPLE","quantity","0.025")),"reason","记录实际取样"),token(sampling),key());
  sampling=service.samplingAction(id(sampling),"submit-to-warehouse",body("reason","提交仓库核对","reauthToken","token"),token(sampling),key());
  assertThat(sampling.path("status").asText()).isEqualTo("PENDING_WAREHOUSE");assertThat(balance()).isEqualByComparingTo(before);
  long warehouse=actor("w");permissions.addAll(Set.of("qms:sampling:warehouse-confirm","wms:inventory:sample-outbound"));as(warehouse);
  sampling=service.samplingAction(id(sampling),"confirm-sample-outbound",body("reason","确认样品实物出库","reauthToken","token"),token(sampling),key());
  assertThat(sampling.path("status").asText()).isEqualTo("COMPLETED");assertThat(balance().subtract(before)).isEqualByComparingTo("-0.125000");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_ledger WHERE source_type='SAMPLING_DETAIL' AND event_type='SAMPLE_OUT' AND source_ref LIKE 'SamplingDetail/%'",Long.class)).isPositive();
  var sample=sampling.path("details").get(0).path("samples").get(0);as(analyst);sample=service.sampleAction(id(sample),"receive",body("receivedAt",Instant.now().toString(),"storageLocation","检验样品柜一层","reason","检验室接收"),token(sample),key());
  var task=request.path("inspectionTask");task=service.bindSample(id(task),body("sampleId",id(sample),"reason","绑定已接收检验样"),token(task),key());assertThat(task.path("sampleId").asText()).isEqualTo(id(sample));assertThat(task.path("allowedActions")).anySatisfy(x->assertThat(x.asText()).isEqualTo("assign"));
  task=service.taskAction(id(task),"assign",body("assignedTo",Long.toString(analyst),"reason","派发检验"),token(task),key());task=service.taskAction(id(task),"start",body("reason","开始检验"),token(task),key());var execution=execution(task);var result=result(task,execution,"2","PASS");review(task,result);assertThat(jdbc.queryForObject("SELECT status FROM qms_inspection_report WHERE auto_generated_request_id=?",String.class,id(request.path("request")))).isEqualTo("DRAFT");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_release_decision WHERE material_lot_id=?",Long.class,lot)).isZero();
 }
 @Test void manualPlannedSamplingRequiresIndependentOutboundThenCreatesOneQcInspectionTask(){
  var generated=createGenerated();var req=generated.path("request");var source=generated.path("samplingTask");
  var manual=service.createSampling(body("inspectionRequestId",id(req),"inspectionRequestItemId",source.path("inspectionRequestItemId").asText(),"samplingPlan","按来料质量标准抽取检验样和留样","requiredPackageCount",1,"reason","确定抽样范围"),key());
  manual=service.updateSamplingPlan(id(manual),body("versionNo",manual.path("versionNo").asLong(),"plannedSamplingQuantity","0.125","plannedUnitId",unit,"reason","确认计划数量"),token(manual),key());
  manual=service.samplingAction(id(manual),"assign",body("assignedTo",Long.toString(analyst),"reason","分配取样责任"),token(manual),key());
  manual=service.samplingAction(id(manual),"start",body("reason","开始取样"),token(manual),key());
  assertThat(manual.path("allowedActions").toString()).contains("submit-to-warehouse").doesNotContain("\"complete\"");
  manual=service.samplingAction(id(manual),"details",body("containerNo","原辅料包装一","sampleQuantity","0.125","unitId",unit,"sampledAt",Instant.now().minusSeconds(2).toString(),"packageResealed",true,"samples",List.of(Map.of("sampleType","TEST_SAMPLE","quantity","0.100"),Map.of("sampleType","RETENTION_SAMPLE","quantity","0.025")),"reason","记录包装复封及样品分配"),token(manual),key());
  var pendingBefore=manual;
  assertThatThrownBy(()->service.samplingAction(id(pendingBefore),"complete",body("reason","不得跳过仓储","reauthToken","token"),token(pendingBefore),key())).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
  var before=balance();
  manual=service.samplingAction(id(manual),"submit-to-warehouse",body("reason","提交仓库实物确认","reauthToken","token"),token(manual),key());
  assertThat(manual.path("status").asText()).isEqualTo("PENDING_WAREHOUSE");
  assertThat(balance()).isEqualByComparingTo(before);
  var awaiting=manual;permissions.addAll(Set.of("qms:sampling:warehouse-confirm","wms:inventory:sample-outbound"));
  as(analyst);
  assertThatThrownBy(()->service.samplingAction(id(awaiting),"confirm-sample-outbound",body("reason","本人不得仓储确认","reauthToken","token"),token(awaiting),key())).isInstanceOf(com.hospital.mes.common.exception.ComplianceException.class);
  long warehouse=actor("w");as(warehouse);
  manual=service.samplingAction(id(manual),"confirm-sample-outbound",body("reason","仓库独立确认实际样品出库","reauthToken","token"),token(manual),key());
  assertThat(manual.path("status").asText()).isEqualTo("COMPLETED");
  assertThat(balance().subtract(before)).isEqualByComparingTo("-0.125000");
  var detail=manual.path("details").get(0);
  assertThat(detail.path("sampleOutboundLedgerId").isNull()).isFalse();
  as(analyst);
  var sample=detail.path("samples").get(0);
  sample=service.sampleAction(id(sample),"receive",body("receivedAt",Instant.now().toString(),"storageLocation","检验室样品柜","reason","检验室核对接收"),token(sample),key());
  assertThat(sample.path("status").asText()).isEqualTo("RECEIVED");
  var inspection=service.createTask(body("inspectionRequestId",id(req),"sampleId",id(sample),"reason","按已批准标准创建检验任务"),key());
  assertThat(inspection.path("sampleId").asText()).isEqualTo(id(sample));
  assertThat(inspection.path("items").size()).isPositive();
  var duplicateSample=sample;
  assertThatThrownBy(()->service.createTask(body("inspectionRequestId",id(req),"sampleId",id(duplicateSample),"reason","重复绑定不得生成"),key())).isInstanceOf(com.hospital.mes.common.exception.MesException.class).hasMessageContaining("already linked");
 }
 @Test void sameSamplerCannotConfirmWarehouseOutbound(){var generated=createGenerated();var sampling=generated.path("samplingTask");sampling=service.samplingAction(id(sampling),"assign",body("assignedTo",Long.toString(analyst),"reason","指定取样员"),token(sampling),key());sampling=service.samplingAction(id(sampling),"start",body("reason","开始取样"),token(sampling),key());sampling=service.samplingAction(id(sampling),"details",body("containerNo","原辅料包装一","sampleQuantity","0.125","unitId",unit,"sampledAt",Instant.now().minusSeconds(2).toString(),"packageResealed",true,"samples",List.of(Map.of("sampleType","TEST_SAMPLE","quantity","0.125")),"reason","记录实际取样"),token(sampling),key());sampling=service.samplingAction(id(sampling),"submit-to-warehouse",body("reason","提交仓库核对","reauthToken","token"),token(sampling),key());permissions.addAll(Set.of("qms:sampling:warehouse-confirm","wms:inventory:sample-outbound"));as(analyst);var pending=sampling;assertThatThrownBy(()->service.samplingAction(id(pending),"confirm-sample-outbound",body("reason","不得本人确认","reauthToken","token"),token(pending),key())).isInstanceOf(com.hospital.mes.common.exception.ComplianceException.class).hasMessageContaining("Independent");}
 @Test void samplingPlanUpdateValidatesStrictCommandPermissionUnitAndQuantity(){
  var generated=createGenerated();var request=generated.path("request");var source=generated.path("samplingTask");
  var task=service.createSampling(body("inspectionRequestId",id(request),"inspectionRequestItemId",source.path("inspectionRequestItemId").asText(),"samplingPlan","DEV incoming sample plan","requiredPackageCount",1,"reason","Create manual sampling task"),key());
  assertThatThrownBy(()->service.updateSamplingPlan(id(task),body("versionNo",task.path("versionNo").asLong(),"plannedSamplingQuantity","0.125","plannedUnitId",unit,"reason","Missing command contract field","unexpected",true),token(task),key())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown field");
  assertThatThrownBy(()->service.updateSamplingPlan(id(task),body("versionNo",task.path("versionNo").asLong(),"plannedSamplingQuantity","0.125","plannedUnitId",unit),token(task),key())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("reason is required");
  assertThatThrownBy(()->service.updateSamplingPlan(id(task),body("versionNo",task.path("versionNo").asLong(),"plannedSamplingQuantity","0.125000000","plannedUnitId",unit,"reason","Invalid precision"),token(task),key())).isInstanceOf(IllegalArgumentException.class);
  var incompatible=units.create(new UnitCommands.Create("IC"+suffix,"Incoming count","COUNT",0),key()).path("id").asText();
  assertThatThrownBy(()->service.updateSamplingPlan(id(task),body("versionNo",task.path("versionNo").asLong(),"plannedSamplingQuantity","0.125","plannedUnitId",incompatible,"reason","Incompatible unit"),token(task),key())).isInstanceOf(RuntimeException.class);
  assertThat(get("qms_sampling_task",id(task)).path("plannedSamplingQuantity").isNull()).isTrue();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE object_type=? AND object_id=? AND action=?",Long.class,"qms_sampling_task",id(task),"SAMPLING_PLAN_UPDATE")).isZero();
 }
 @Test void assignedSamplingTaskPlanUpdateIsAuditedIdempotentAndLockedAfterStart(){
  var generated=createGenerated();var request=generated.path("request");var source=generated.path("samplingTask");
  var task=service.createSampling(body("inspectionRequestId",id(request),"inspectionRequestItemId",source.path("inspectionRequestItemId").asText(),"samplingPlan","DEV incoming sample plan","requiredPackageCount",1,"reason","Create manual sampling task"),key());
  task=service.samplingAction(id(task),"assign",body("assignedTo",Long.toString(analyst),"reason","Assign sampler"),token(task),key());
  permissions.remove("qms:sampling:assign");as(analyst);var deniedTask=task;
  assertThatThrownBy(()->service.updateSamplingPlan(id(deniedTask),body("versionNo",deniedTask.path("versionNo").asLong(),"plannedSamplingQuantity","0.125","plannedUnitId",unit,"reason","Unauthorized update"),token(deniedTask),key())).isInstanceOf(com.hospital.mes.common.exception.PermissionException.class);
  permissions.add("qms:sampling:assign");as(analyst);
  var command=body("versionNo",task.path("versionNo").asLong(),"plannedSamplingQuantity","0.125","plannedUnitId",unit,"reason","Set bounded DEV sample quantity");String updateKey=key();
  var updated=service.updateSamplingPlan(id(task),command,token(task),updateKey);
  assertThat(new BigDecimal(updated.path("plannedSamplingQuantity").asText())).isEqualByComparingTo("0.125");assertThat(updated.path("plannedUnitId").asText()).isEqualTo(unit);assertThat(updated.path("versionNo").asLong()).isEqualTo(task.path("versionNo").asLong()+1);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE object_type=? AND object_id=? AND action=?",Long.class,"qms_sampling_task",id(updated),"SAMPLING_PLAN_UPDATE")).isEqualTo(1);
  var replay=service.updateSamplingPlan(id(task),command,token(task),updateKey);assertThat(replay.path("versionNo").asLong()).isEqualTo(updated.path("versionNo").asLong());
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE object_type=? AND object_id=? AND action=?",Long.class,"qms_sampling_task",id(updated),"SAMPLING_PLAN_UPDATE")).isEqualTo(1);
  var current=updated;String excessKey=key();
  assertThatThrownBy(()->service.updateSamplingPlan(id(current),body("versionNo",current.path("versionNo").asLong(),"plannedSamplingQuantity","1000","plannedUnitId",unit,"reason","Excess quantity must fail"),token(current),excessKey)).isInstanceOf(RuntimeException.class);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE object_type=? AND object_id=? AND action=?",Long.class,"qms_sampling_task",id(updated),"SAMPLING_PLAN_UPDATE")).isEqualTo(1);
  assertThat(new BigDecimal(get("qms_sampling_task",id(updated)).path("plannedSamplingQuantity").asText())).isEqualByComparingTo("0.125");
  var started=service.samplingAction(id(updated),"start",body("reason","Start sampling"),token(updated),key());
  assertThatThrownBy(()->service.updateSamplingPlan(id(started),body("versionNo",started.path("versionNo").asLong(),"plannedSamplingQuantity","0.100","plannedUnitId",unit,"reason","Started task cannot be edited"),token(started),key())).isInstanceOf(RuntimeException.class);
 }
 private com.fasterxml.jackson.databind.JsonNode createGenerated(){var source=jdbc.queryForMap("SELECT CAST(ri.receipt_id AS CHAR) receipt_id,CAST(ri.id AS CHAR) item_id FROM wms_material_receipt_item ri JOIN md_material_lot ml ON ml.receipt_item_id=ri.id WHERE ml.id=?",lot);var request=service.createInitialRequestFromReceipt((String)source.get("receipt_id"),(String)source.get("item_id"),key());return service.confirmAndGenerate(id(request),body("qcSpecificationVersionId",version,"plannedSamplingQuantity","0.125","plannedSamplingUnitId",unit,"requiredPackageCount",1,"samplingPlan","按批准方案抽取一件","reason","确认首次请验计划"),token(request),key());}
 private BigDecimal balance(){return jdbc.queryForObject("SELECT COALESCE(SUM(delta_qty),0) FROM wms_inventory_ledger WHERE material_lot_id=?",BigDecimal.class,lot);}
}
