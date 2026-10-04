package com.hospital.mes.incoming;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
/** Additional contract scenarios. Only unique inherited fixtures, transaction rollback, no schema or seed writes. */
@AutoConfigureMockMvc
class IncomingQualityAcceptanceIT extends IncomingQualityFixture {
 @Autowired MockMvc mvc;
 @Test void signedRevisionReportSupersessionAndRejectPreserveEveryPredecessor(){
  var request=request();var sample=sample(request);var task=task(request,sample);var execution=execution(task);var first=result(task,execution,"2","PASS");
  var corrected=service.result(id(first),true,body("testExecutionId",id(execution),"resultNumeric","2.1","resultUnitId",unit,"resultConclusion","PASS","reasonForChange","Verified transcription correction","reauthToken","token"),token(get("qms_inspection_task",id(task))),key());
  assertThat(corrected.path("revisionNo").asInt()).isEqualTo(2);assertThat(corrected.path("previousRevisionId").asText()).isEqualTo(id(first));
  assertThat(jdbc.queryForObject("SELECT result_numeric FROM qms_test_result_revision WHERE id=?",java.math.BigDecimal.class,id(first))).isEqualByComparingTo("2");
  assertThat(verifier.verify(1,Long.parseLong(first.path("signatureId").asText()))).isTrue();review(task,corrected);var originalReport=report(request);
  assertThatThrownBy(()->service.reportAction(id(originalReport),"review",body("reason","Forbidden reopen"),token(originalReport),key())).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
  as(analyst);var replacement=service.createReport(body("inspectionRequestId",id(request),"supersedesReportId",id(originalReport),"reason","Controlled report replacement"),key());as(reviewer);replacement=service.reportAction(id(replacement),"review",body("reason","Independent review"),token(replacement),key());as(approver);replacement=service.reportAction(id(replacement),"approve",body("reason","Approve replacement","reauthToken","token"),token(replacement),key());
  assertThat(replacement.path("supersedesReportId").asText()).isEqualTo(id(originalReport));assertThat(verifier.verify(1,Long.parseLong(originalReport.path("approvalSignatureId").asText()))).isTrue();
  as(qa);var gate=service.releaseReview(lot);var released=service.decideRelease(lot,body("decision","RELEASED","releaseBasis","FULL_INSPECTION","inspectionReportId",id(replacement),"reason","Independent material release","reauthToken","token"),token(gate),key());
  var updated=service.releaseReview(lot);var rejected=service.decideRelease(lot,body("decision","REJECTED","releaseBasis","FULL_INSPECTION","inspectionReportId",id(replacement),"supersedesDecisionId",id(released),"reason","Controlled superseding quality rejection","reauthToken","token"),token(updated),key());
  assertThat(rejected.path("supersedesDecisionId").asText()).isEqualTo(id(released));assertThat(verifier.verify(1,Long.parseLong(released.path("signatureId").asText()))).isTrue();assertThat(verifier.verify(1,Long.parseLong(rejected.path("signatureId").asText()))).isTrue();
  assertThat(jdbc.queryForObject("SELECT decision FROM qms_release_decision WHERE id=?",String.class,id(released))).isEqualTo("RELEASED");assertThat(jdbc.queryForMap("SELECT quality_status,inventory_status FROM md_material_lot WHERE id=?",lot)).containsEntry("quality_status","REJECTED").containsEntry("inventory_status","BLOCKED");assertThat(queries.evaluate(1,Long.parseLong(lot),"WEIGH",Instant.now()).path("eligible").asBoolean()).isFalse();
 }
 @Test void otherPurposeRequiresSignedPlanAndEveryContainerBeforeCompletion(){
  var source=jdbc.queryForMap("SELECT r.supplier_id,r.warehouse_id,i.location_id FROM md_material_lot l JOIN wms_material_receipt_item i ON i.id=l.receipt_item_id JOIN wms_material_receipt r ON r.id=i.receipt_id WHERE l.id=?",lot);
  var receiptItem=new com.hospital.mes.wms.domain.WmsCommands.ReceiptItemInput(material,"FIVE"+suffix,"SUPF"+suffix,null,LocalDate.now(ZoneOffset.UTC).minusDays(1).toString(),LocalDate.now(ZoneOffset.UTC).plusYears(1).toString(),null,"10",unit,null,5L,source.get("location_id").toString(),null,true,true,true,true,true);
  var received=wms.createReceipt(new com.hospital.mes.wms.domain.WmsCommands.ReceiptCreate("FIVE-R"+suffix,source.get("supplier_id").toString(),null,null,source.get("warehouse_id").toString(),true,List.of(receiptItem)),key(),true);lot=received.path("items").get(0).path("materialLotId").asText();
  var request=service.createRequest(body("materialLotId",lot,"qcSpecificationVersionId",version,"requestType","INITIAL","reason","Five-container inspection","requestedQuantity","10","requestedUnitId",unit,"requestedPackageCount",5,"requestedDate",LocalDate.now(ZoneOffset.UTC).toString(),"priority","NORMAL"),key());request=service.requestAction(id(request),"submit",body("reason","Submit"),token(request),key());request=service.requestAction(id(request),"accept",body("reason","Accept"),token(request),key());
  var task=service.createSampling(body("inspectionRequestId",id(request),"inspectionRequestItemId",request.path("items").get(0).path("id").asText(),"samplingPlan","Five controlled containers","requiredPackageCount",5,"otherSampleName","Method suitability","otherSampleReason","Approved analytical verification","reason","Plan"),key());
  var unsigned=task;assertThatThrownBy(()->service.samplingAction(id(unsigned),"approve-plan",body("reason","Self approval","reauthToken","token"),token(unsigned),key())).isInstanceOf(com.hospital.mes.common.exception.ComplianceException.class);
  as(qa);task=service.samplingAction(id(task),"approve-plan",body("reason","Independent purpose approval","reauthToken","token"),token(task),key());long planSignature=task.path("planSignatureId").asLong();as(analyst);task=service.samplingAction(id(task),"assign",body("assignedTo",Long.toString(analyst),"reason","Assign"),token(task),key());task=service.samplingAction(id(task),"start",body("reason","Start"),token(task),key());for(int container=1;container<=4;container++)task=detail(task,"C"+container);var incomplete=task;
  assertThatThrownBy(()->service.samplingAction(id(incomplete),"complete",body("reason","Missing container","reauthToken","token"),token(incomplete),key())).isInstanceOf(com.hospital.mes.common.exception.ComplianceException.class);
  assertThat(get("qms_sampling_task",id(task)).path("status").asText()).isEqualTo("IN_PROGRESS");task=detail(task,"C5");task=service.samplingAction(id(task),"complete",body("reason","All containers sampled","reauthToken","token"),token(task),key());assertThat(task.path("details")).hasSize(5);assertThat(verifier.verify(1,planSignature)).isTrue();
  for(var d:task.path("details")){var s=d.path("samples").get(0);assertThat(s.path("sampleType").asText()).isEqualTo("OTHER_APPROVED");assertThat(s.path("samplingDetailId").asText()).isEqualTo(id(d));var label=service.sampleLabel(id(s),body("reason","Controlled label"),token(s),key());assertThat(label.path("containerNo").asText()).isEqualTo(d.path("containerNo").asText());}
 }
 private JsonNode detail(JsonNode task,String container){return service.samplingAction(id(task),"details",body("containerNo",container,"sampleQuantity","0.5","unitId",unit,"sampledAt",Instant.now().minusSeconds(2).toString(),"packageResealed",true,"samples",List.of(Map.of("sampleType","OTHER_APPROVED","quantity","0.5","storageLocation","QC controlled store")),"reason","Actual sample"),token(task),key());}
 @Test void actualHttpRejectsMissingPermissionClosedDtoAndWrongOrganization() throws Exception {
  var request=request();String url="/api/v1/quality/inspection-requests/"+id(request);
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url).with(user("incoming").authorities(new SimpleGrantedAuthority("qms:test:view")))).andExpect(status().isForbidden());
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url).with(user("incoming").authorities(new SimpleGrantedAuthority("qms:inspection-request:view")))).andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(id(request))).andExpect(jsonPath("$.traceId").exists());
  var command=body("materialLotId",lot,"qcSpecificationVersionId",version,"requestType","INITIAL","reason","Closed request","requestedQuantity","1","requestedUnitId",unit,"requestedPackageCount",1,"requestedDate",LocalDate.now(ZoneOffset.UTC).toString(),"priority","NORMAL","status","COMPLETED");
  mvc.perform(post("/api/v1/quality/inspection-requests").with(user("incoming").authorities(new SimpleGrantedAuthority("qms:inspection-request:create"))).header("Idempotency-Key",key()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(command))).andExpect(status().isBadRequest());
  org.mockito.Mockito.when(contexts.current()).thenReturn(new com.hospital.mes.audit.application.CurrentPlatformContext(2,analyst,Set.of("INCOMING_TEST"),permissions,"session","scope"));
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url).with(user("incoming").authorities(new SimpleGrantedAuthority("qms:inspection-request:view")))).andExpect(status().isNotFound());
 }
}
