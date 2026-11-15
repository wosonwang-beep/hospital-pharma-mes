package com.hospital.mes.incoming;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

/** New rows only, inherited transaction rollback. Catches wrong sample/report joins and fake filters. */
class InspectionTaskListIT extends IncomingQualityFixture {
 private JsonNode page(String keyword,Map<String,String> filters,int page,int size){
  var f=new HashMap<>(filters);f.put("projection","t1");
  return service.list("qms_inspection_task","qms:test:view",page,size,keyword,f);
 }
 @Test void sampleIdentityFiltersCountAndLegacyDetailRemainConsistent(){
  var request=request();var sample=sample(request);var task=task(request,sample);
  var list=page(lot,Map.of("inspectionRequestId",id(request)),0,20);
  // Keyword is the actual lot number, not its database ID.
  String lotNo=jdbc.queryForObject("SELECT lot_no FROM md_material_lot WHERE id=?",String.class,lot);
  list=page(lotNo,Map.of("assignedTo",String.valueOf(analyst),"status","IN_PROGRESS","requestType","INITIAL"),0,1);
  assertThat(list.path("total").asInt()).isEqualTo(1);
  var row=list.path("items").get(0);
  assertThat(row.path("id").asText()).isEqualTo(id(task));
  assertThat(row.path("sampleName").asText()).isEqualTo("Incoming salt");
  assertThat(row.path("lotNo").asText()).isEqualTo(lotNo);
  assertThat(row.path("sampleNo").asText()).isEqualTo(sample.path("sampleNo").asText());
  assertThat(row.path("reportNo").isNull()).isTrue();
  assertThat(row.path("overallResult").isNull()).isTrue();
  assertThat(row.path("allowedActions").toString()).contains("execute","view");
  assertThat(list.path("inspectors").toString()).contains(String.valueOf(analyst));
  assertThat(page(lotNo,Map.of("assignedTo",String.valueOf(reviewer)),0,20).path("total").asInt()).isZero();
  assertThat(page(lotNo,Map.of(),1,1).path("items")).isEmpty();
  assertThat(page("%_",Map.of(),0,20).path("total").asInt()).isZero();
  var legacy=service.list("qms_inspection_task","qms:test:view",0,20,null,Map.of("inspectionRequestId",id(request)));
  assertThat(legacy.path("items").get(0).path("items")).isNotEmpty();
 }
 @Test void reportNumberComesFromTaskItemsAndOriginalFailureNeverBecomesPass(){
  var request=request();var sample=sample(request);var task=task(request,sample);
  var execution=execution(task);var result=result(task,execution,"4","FAIL");review(task,result);
  as(analyst);var report=service.createReport(body("inspectionRequestId",id(request),"reason","Aggregate results"),key());
  var list=page(report.path("reportNo").asText(),Map.of(),0,20);
  assertThat(list.path("total").asInt()).isEqualTo(1);
  var row=list.path("items").get(0);
  assertThat(row.path("id").asText()).isEqualTo(id(task));
  assertThat(row.path("reportNo").asText()).isEqualTo(report.path("reportNo").asText());
  assertThat(row.path("reportNo").asText()).isNotEqualTo(task.path("inspectionTaskNo").asText());
  assertThat(row.path("overallResult").asText()).isEqualTo("FAIL");
  assertThat(row.path("reportIds").get(0).asText()).isEqualTo(id(report));
  var second=service.createReport(body("inspectionRequestId",id(request),"reason","Second draft aggregation"),key());
  var multiple=page(report.path("reportNo").asText(),Map.of(),0,20).path("items").get(0);
  assertThat(multiple.path("reportNo").asText()).isEqualTo(report.path("reportNo").asText()+"、"+second.path("reportNo").asText());
  assertThat(multiple.path("reportIds")).hasSize(2);
  assertThat(multiple.path("reportId").isNull()).isTrue();
  // Another task on this same request must not inherit the first task's report.
  jdbc.update("INSERT INTO qms_inspection_task(org_id,created_by,updated_by,inspection_task_no,inspection_request_id,sample_id,status,record_status) VALUES(1,?,?,?,?,?,'CREATED','DRAFT')",analyst,analyst,"JY-"+suffix,id(request),id(sample));
  String other=jdbc.queryForObject("SELECT CAST(id AS CHAR) FROM qms_inspection_task WHERE inspection_task_no=?",String.class,"JY-"+suffix);
  var sameRequest=page(null,Map.of("inspectionRequestId",id(request)),0,20);
  assertThat(sameRequest.path("total").asInt()).isEqualTo(2);
  var unrelated=sameRequest.path("items").get(0);
  assertThat(unrelated.path("id").asText()).isEqualTo(other);
  assertThat(unrelated.path("reportIds")).isEmpty();
  assertThat(unrelated.path("reportNo").isNull()).isTrue();
 }
 @Test void permissionsAndOrganizationCannotLeakRowsOrExecution(){
  var request=request();var sample=sample(request);var task=task(request,sample);
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,analyst,Set.of(),Set.of("qms:test:view"),"s","list-view"));
  var row=page(null,Map.of("inspectionRequestId",id(request)),0,20).path("items").get(0);
  assertThat(row.path("allowedActions").toString()).isEqualTo("[\"view\"]");
  when(contexts.current()).thenReturn(new CurrentPlatformContext(2,analyst,Set.of(),Set.of("qms:test:view"),"s","list-org"));
  assertThat(page(null,Map.of("inspectionRequestId",id(request)),0,20).path("total").asInt()).isZero();
  assertThat(page(null,Map.of("inspectionRequestId",id(request)),0,20).path("items")).isEmpty();
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,analyst,Set.of(),Set.of(),"s","list-denied"));
  assertThatThrownBy(()->page(null,Map.of(),0,20)).isInstanceOf(com.hospital.mes.common.exception.MesException.class);
 }
}
