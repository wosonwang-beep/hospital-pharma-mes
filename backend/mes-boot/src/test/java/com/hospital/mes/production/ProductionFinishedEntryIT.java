package com.hospital.mes.production;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.production.application.NavigationQueryService;
import com.hospital.mes.qms.application.FinishedTestReadService;
import com.hospital.mes.qms.application.ProductionQualityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

/** Real native MariaDB fixture chain; inherited transaction rolls back unique records only. */
class ProductionFinishedEntryIT extends FinishedGoodsLifecycleIT {
 private void orgTwo(){org.mockito.Mockito.when(contexts.current()).thenReturn(new com.hospital.mes.audit.application.CurrentPlatformContext(2,author,Set.of("INCOMING_TEST"),permissions,"entry-other-org","entry-other-org-"+suffix));}
 @Autowired NavigationQueryService entry; @Autowired FinishedTestReadService finishedTests;
 @Test void scopedExecutionAndBalanceSelectorsUseRealIdsWithoutGenericBatchPermission(){
  var b=approvedBatch();String batch=id(b),unitId=b.path("executionUnits").get(0).path("id").asText();
  permissions.remove("production:batch:view");as(author);
  var units=entry.executions(0,1,Map.of("mainBatchId",batch));assertThat(units.total()).isEqualTo(1);assertThat(units.items().getFirst().path("id").asText()).isEqualTo(unitId);assertThat(units.items().getFirst().path("batchNo").asText()).isEqualTo(b.path("batchNo").asText());
  var balance=entry.batches("balance",0,1,Map.of("mainBatchId",batch));assertThat(balance.total()).isEqualTo(1);assertThat(entry.batch("balance",batch).has("processSnapshot")).isFalse();assertThat(entry.batch("balance",batch).path("versionNo").asLong()).isEqualTo(b.path("versionNo").asLong());
  assertThatThrownBy(()->production.batch(batch)).isInstanceOf(com.hospital.mes.common.exception.PermissionException.class);
  FrozenSchemaAssertions.assertSchema(json,"1.0.23","ExecutionEntryPage",json.valueToTree(units));FrozenSchemaAssertions.assertSchema(json,"1.0.23","BatchEntryPage",json.valueToTree(balance)); orgTwo();assertThat(entry.executions(0,20,Map.of("mainBatchId",batch)).total()).isZero();assertThat(entry.batches("balance",0,20,Map.of("mainBatchId",batch)).total()).isZero();assertThatThrownBy(()->entry.batch("balance",batch)).isInstanceOf(NoSuchElementException.class);as(author);
  permissions.remove("mes:execution:view");as(author);assertThatThrownBy(()->entry.executions(0,20,Map.of())).isInstanceOf(com.hospital.mes.common.exception.PermissionException.class);
  permissions.remove("balance:view");as(author);assertThatThrownBy(()->entry.batches("balance",0,20,Map.of())).isInstanceOf(com.hospital.mes.common.exception.PermissionException.class);
 }
 @Test void finishedOriginAndSampleFiltersRunBeforePaginationAndExcludeOriginalProductionQc(){
  var b=completedProductionBatch(()->{});var receipt=confirmedFinishedReceipt(b);var request=acceptedFinishedRequest(receipt);String batch=id(b);
  var first=createEntryTest(request,"A");var fresh=finishedInspection.getRequest(id(request));var second=createEntryTest(fresh,"B");
  var all=finishedTests.list(0,1,Map.of("mainBatchId",batch));assertThat(all.total()).isEqualTo(2);assertThat(all.items()).hasSize(1);var next=finishedTests.list(1,1,Map.of("mainBatchId",batch));assertThat(next.items().getFirst().path("id").asText()).isNotEqualTo(all.items().getFirst().path("id").asText());
  assertThat(finishedTests.list(0,1,Map.of("sampleId",first.path("sampleId").asText())).total()).isEqualTo(1);assertThat(finishedTests.list(0,1,Map.of("keyword","FES_B"+suffix)).items().getFirst().path("id").asText()).isEqualTo(id(second));
  assertThat(finishedTests.get(id(first)).path("sampleId").asText()).isEqualTo(first.path("sampleId").asText());
  FrozenSchemaAssertions.assertSchema(json,"1.0.23","FinishedTestEntryPage",json.valueToTree(all));orgTwo();assertThat(finishedTests.list(0,20,Map.of("mainBatchId",batch)).total()).isZero();assertThatThrownBy(()->finishedTests.get(id(first))).isInstanceOf(NoSuchElementException.class);as(author);
  String original=jdbc.queryForObject("SELECT t.id FROM qms_production_test_instance t JOIN qms_sample s ON s.id=t.sample_id WHERE s.main_batch_id=? AND s.source_ref NOT LIKE 'FinishedInspectionRequest:%' ORDER BY t.id LIMIT 1",Long.class,batch)+"";
  assertThatThrownBy(()->finishedTests.get(original)).isInstanceOf(NoSuchElementException.class);
  assertThatThrownBy(()->finishedTests.list(0,20,Map.of("unknown","x"))).isInstanceOf(IllegalArgumentException.class);
  permissions.remove("qms:test:view");as(author);assertThatThrownBy(()->finishedTests.list(0,1,Map.of())).isInstanceOf(com.hospital.mes.common.exception.PermissionException.class);
 }
 private JsonNode createEntryTest(JsonNode request,String code){as(author);var sampling=finishedInspection.sample(id(request),body("versionNo",request.path("versionNo").asLong(),"samplingNo","FER_"+code+suffix,"sampleNo","FES_"+code+suffix,"sampleType","TEST_SAMPLE","samplingLocation","Actual finished shelf","quantity","1","unitId",unit,"samplingMethod","Approved representative sample","reason","Actual entry verification","signature",Map.of("reauthToken","token")),token(request),key());var s=productionQuality.get(ProductionQualityService.SAMPLE,sampling.path("sampleId").asText());s=productionQuality.receiveSample(id(s),body("versionNo",0,"reason","QC receives actual finished sample"),token(s),key());String item=request.path("specificationSnapshot").path("specification").path("items").get(0).path("specificationItemId").asText();return productionQuality.createTest(body("sampleId",id(s),"specificationItemId",item,"reason","Actual finished test instance"),key());}
 @Test void warehouseAndQaIndexesUseExistingFactsAndScopedPermissions(){
  var b=completedProductionBatch(()->{});String batch=id(b);
  as(author);var draft=finishedGoods.createInbound(body("requestNo","ENTRY_DRAFT"+suffix,"mainBatchId",batch,"reason","Additional draft only"),key());
  assertThat(finishedGoods.listInbound(0,1,Map.of("mainBatchId",batch,"receivingOnly","true")).total()).isZero();
  draft=finishedGoods.inboundAction(id(draft),"submit",body("versionNo",draft.path("versionNo").asLong(),"reason","Submit actual receipt"),token(draft),key());
  var wh=wms.maintenance("Warehouse",null,new com.hospital.mes.wms.domain.WmsCommands.WarehouseCreate("ENTRYW"+suffix,"Entry finished warehouse","FINISHED"),null,key());var loc=wms.maintenance("Location",null,new com.hospital.mes.wms.domain.WmsCommands.LocationCreate(id(wh),"ENTRYL"+suffix,"Entry receiving location"),null,key());
  as(qa);var receipt=finishedGoods.inboundAction(id(draft),"confirm",body("versionNo",draft.path("versionNo").asLong(),"locationId",id(loc),"reason","Actual received output","signature",Map.of("reauthToken","token")),token(draft),key());
  var list=finishedGoods.listInbound(0,1,Map.of("mainBatchId",batch,"receivingOnly","true"));assertThat(list.total()).isEqualTo(1);assertThat(list.items().getFirst().path("id").asText()).isEqualTo(id(receipt));assertThat(finishedGoods.listInbound(0,20,Map.of("mainBatchId",batch)).total()).isEqualTo(1);
  permissions.remove("production:batch:view");as(qa);assertThat(entry.batches("qa-review",0,1,Map.of("mainBatchId",batch)).total()).isEqualTo(1);assertThat(entry.batches("release",0,1,Map.of("mainBatchId",batch)).total()).isEqualTo(1);assertThat(entry.batch("qa-review",batch).has("executionUnits")).isFalse();
  permissions.remove("qa:batch-review");as(qa);assertThatThrownBy(()->entry.batches("release",0,1,Map.of())).isInstanceOf(com.hospital.mes.common.exception.PermissionException.class);
  assertThatThrownBy(()->finishedGoods.listInbound(0,20,Map.of("receivingOnly","yes"))).isInstanceOf(IllegalArgumentException.class);
 }
}
