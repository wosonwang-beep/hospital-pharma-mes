package com.hospital.mes.incoming;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hospital.mes.audit.domain.AuditCommand;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** G4-BATCH-A: confirmed receipt -> submitted INITIAL request -> QC atomic two-document generation. */
class IncomingCrossroleBatchAIT extends IncomingQualityFixture {

 @Test void confirmedRequiredReceiptCreatesOneSubmittedInitialRequestAndReplaysSameKey() {
  var source=receiptSource();
  String commandKey=key();

  var created=service.createInitialRequestFromReceipt(source.receiptId(),source.itemId(),commandKey);
  var replay=service.createInitialRequestFromReceipt(source.receiptId(),source.itemId(),commandKey);

  assertThat(replay.path("id").asText()).isEqualTo(created.path("id").asText());
  assertThat(replay.path("requestNo").asText()).isEqualTo(created.path("requestNo").asText());
  assertThat(created.path("status").asText()).isEqualTo("SUBMITTED");
  assertThat(created.path("requestType").asText()).isEqualTo("INITIAL");
  assertThat(created.path("materialLotId").asText()).isEqualTo(lot);
  assertThat(new BigDecimal(created.path("requestedQuantity").asText())).isEqualByComparingTo("10");
  assertThat(created.path("requestedUnitId").asText()).isEqualTo(unit);
  assertThat(created.path("requestedPackageCount").asInt()).isEqualTo(2);
  assertThat(created.path("qcSpecificationVersionId").isNull()).isTrue();
  assertThat(created.path("specificationContentHash").isNull()).isTrue();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_inspection_request WHERE org_id=1 AND material_lot_id=? AND request_type='INITIAL'",Long.class,lot)).isEqualTo(1L);
  assertThatThrownBy(()->service.createInitialRequestFromReceipt(source.receiptId(),source.itemId(),key()))
    .hasMessageContaining("INITIAL");
 }

 @Test void qcConfirmationFreezesApprovedSpecificationAndAtomicallyCreatesExactlyTwoPlannedDocuments() {
  var source=receiptSource();
  var request=service.createInitialRequestFromReceipt(source.receiptId(),source.itemId(),key());
  var command=body("qcSpecificationVersionId",version,"plannedSamplingQuantity","0.12500000",
    "plannedSamplingUnitId",unit,"requiredPackageCount",1,"samplingPlan","按批准方案随机抽取一件",
    "reason","确认首次请验计划");

  var confirmed=service.confirmAndGenerate(id(request),command,token(request),key());

  assertThat(confirmed.path("request").path("status").asText()).isEqualTo("ACCEPTED");
  assertThat(confirmed.path("request").path("qcSpecificationVersionId").asText()).isEqualTo(version);
  assertThat(confirmed.path("samplingTask").path("status").asText()).isEqualTo("PLANNED");
  assertThat(confirmed.path("samplingTask").path("plannedSamplingQuantity").asText()).isEqualTo("0.12500000");
  assertThat(confirmed.path("samplingTask").path("plannedUnitId").asText()).isEqualTo(unit);
  assertThat(confirmed.path("inspectionTask").path("status").asText()).isEqualTo("CREATED");
  assertThat(confirmed.path("inspectionTask").path("sampleId").isNull()).isTrue();
  assertThat(confirmed.path("inspectionTask").path("items")).hasSize(1);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_sampling_task WHERE qc_auto_generated_request_id=?",Long.class,id(request))).isEqualTo(1L);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_inspection_task WHERE qc_auto_generated_request_id=?",Long.class,id(request))).isEqualTo(1L);
  assertThat(jdbc.queryForObject("SELECT planned_sampling_quantity FROM qms_sampling_task WHERE qc_auto_generated_request_id=?",BigDecimal.class,id(request))).isEqualByComparingTo("0.12500000");
 }

 @Test void qcConfirmationAuditFailureRollsBackRequestSamplingTaskInspectionTaskAndFrozenItems() {
  var source=receiptSource();
  var request=service.createInitialRequestFromReceipt(source.receiptId(),source.itemId(),key());
  doAnswer(invocation->{var command=(AuditCommand)invocation.getArgument(0);if(command.action().equals("CONFIRM_AND_GENERATE_INCOMING_INSPECTION"))throw new IllegalStateException("injected cross-role audit failure");return invocation.callRealMethod();}).when(audit).append(any());
  var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);

  assertThatThrownBy(()->tx.execute(ignored->service.confirmAndGenerate(id(request),
    body("qcSpecificationVersionId",version,"plannedSamplingQuantity","0.125","plannedSamplingUnitId",unit,
      "requiredPackageCount",1,"samplingPlan","按批准方案随机抽取一件","reason","确认首次请验计划"),
    token(request),key()))).hasMessageContaining("injected cross-role audit failure");

  assertThat(jdbc.queryForObject("SELECT status FROM qms_inspection_request WHERE id=?",String.class,id(request))).isEqualTo("SUBMITTED");
  assertThat(jdbc.queryForObject("SELECT qc_specification_version_id FROM qms_inspection_request WHERE id=?",Long.class,id(request))).isNull();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_sampling_task WHERE inspection_request_id=?",Long.class,id(request))).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_inspection_task WHERE inspection_request_id=?",Long.class,id(request))).isZero();
 }

 @Test void waitingSampleTaskIsReadOnlyAndCannotBeAssignedOrStarted() {
  var source=receiptSource();
  var request=service.createInitialRequestFromReceipt(source.receiptId(),source.itemId(),key());
  var generated=service.confirmAndGenerate(id(request),body("qcSpecificationVersionId",version,
    "plannedSamplingQuantity","0.125","plannedSamplingUnitId",unit,"requiredPackageCount",1,
    "samplingPlan","按批准方案随机抽取一件","reason","确认首次请验计划"),token(request),key());
  var task=generated.path("inspectionTask");

  assertThat(task.path("allowedActions").toString()).doesNotContain("\"assign\"","\"start\"","\"execute\"","\"review\"");
  assertThatThrownBy(()->service.taskAction(id(task),"assign",body("assignedTo",Long.toString(analyst),"reason","派发检验"),token(task),key()))
    .hasMessageContaining("sample");
  assertThat(jdbc.queryForObject("SELECT sample_id FROM qms_inspection_task WHERE id=?",Long.class,id(task))).isNull();
 }

 @Test void legacyRequestWithFrozenSpecificationStillUsesExistingAcceptPath() {
  var accepted=request();
  assertThat(accepted.path("status").asText()).isEqualTo("ACCEPTED");
  assertThat(accepted.path("qcSpecificationVersionId").asText()).isEqualTo(version);
  assertThat(accepted.path("specificationContentHash").asText()).hasSize(64);
 }

 @Test void warehouseAndQcCommandsRejectCrossOrganizationMissingPermissionAndStaleVersion() {
  var source=receiptSource();
  var originalPermissions=new HashSet<>(permissions);
  permissions.remove("wms:receipt:confirm");as(analyst);
  assertThatThrownBy(()->service.createInitialRequestFromReceipt(source.receiptId(),source.itemId(),key()))
    .isInstanceOf(com.hospital.mes.common.exception.PermissionException.class);
  permissions.clear();permissions.addAll(originalPermissions);as(analyst);
  var request=service.createInitialRequestFromReceipt(source.receiptId(),source.itemId(),key());
  when(contexts.current()).thenReturn(new CurrentPlatformContext(2,analyst,Set.of("INCOMING_TEST"),permissions,"foreign-session","foreign-scope"));
  assertThatThrownBy(()->service.confirmAndGenerate(id(request),body("qcSpecificationVersionId",version,
    "plannedSamplingQuantity","0.125","plannedSamplingUnitId",unit,"requiredPackageCount",1,
    "samplingPlan","按批准方案随机抽取一件","reason","确认首次请验计划"),token(request),key()))
    .isInstanceOf(java.util.NoSuchElementException.class);
  as(analyst);
  assertThatThrownBy(()->service.confirmAndGenerate(id(request),body("qcSpecificationVersionId",version,
    "plannedSamplingQuantity","0.125","plannedSamplingUnitId",unit,"requiredPackageCount",1,
    "samplingPlan","按批准方案随机抽取一件","reason","确认首次请验计划"),"\"99\"",key()))
    .isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
 }

 private ReceiptSource receiptSource() {
  var row=jdbc.queryForMap("SELECT CAST(ri.receipt_id AS CHAR) receipt_id,CAST(ri.id AS CHAR) item_id FROM wms_material_receipt_item ri JOIN md_material_lot ml ON ml.receipt_item_id=ri.id WHERE ml.id=?",lot);
  return new ReceiptSource((String)row.get("receipt_id"),(String)row.get("item_id"));
 }

 private record ReceiptSource(String receiptId,String itemId) {}
}
