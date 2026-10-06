package com.hospital.mes.production;
import static org.assertj.core.api.Assertions.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.wms.application.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

/** Test-owned fixtures and regulated facts roll back with the existing native fixture. */
class WmsRequestManagementIT extends IncomingProductionIT {
 @Autowired MaterialRequestService requests;@Autowired WmsManagementQueryService management;
 @Autowired com.hospital.mes.masterdata.application.UnitConversionService conversions;
 @Autowired com.hospital.mes.masterdata.application.UnitService requestUnits;
 @Autowired org.mybatis.spring.SqlSessionTemplate requestSql;
 @BeforeEach void requestPermissions(){for(String a:List.of("view","create","update","submit","cancel"))permissions.add("wms:request:"+a);as(author);}
 JsonNode demand(JsonNode batch,String qty){String formula=batch.path("processSnapshot").path("snapshot").path("process").path("formula").path("items").get(0).path("formulaItemId").asText();return requests.create(body("requestNo","RQ"+UUID.randomUUID(),"mainBatchId",id(batch),"reason","Actual material demand","items",List.of(Map.of("formulaItemId",formula,"requestedQty",qty,"unitId",unit))),key());}
 JsonNode linkedIssue(JsonNode batch,JsonNode request,String qty){var line=request.path("items").get(0);return stock.createIssue(body("issueNo","OUT"+UUID.randomUUID(),"mainBatchId",id(batch),"materialRequestId",id(request),"reason","Linked handover","items",List.of(Map.of("materialLotId",lot,"formulaItemId",line.path("formulaItemId").asText(),"materialRequestItemId",id(line),"issuedQty",qty,"unitId",unit))),key());}
 JsonNode submit(JsonNode r){return requests.submit(id(r),body("versionNo",r.path("versionNo").asLong(),"reason","Submit production demand"),token(r),key());}
 void reserve(JsonNode batch,JsonNode request,String qty){stock.reserve(id(batch),body("reason","Existing reservation","items",List.of(Map.of("materialLotId",lot,"formulaItemId",request.path("items").get(0).path("formulaItemId").asText(),"reservedQty",qty,"unitId",unit))),token(batch),key());}
 @Test void managementRequestLifecycleRejectsStateAndRetainsSnapshotAndReplay(){
  var batch=releasedBatch();var request=demand(batch,"6");var line=request.path("items").get(0);assertThat(request.path("processSnapshotId").asText()).isEqualTo(batch.path("processSnapshotId").asText());assertThat(request.path("productName").asText()).isNotBlank();
  var edited=requests.edit(id(request),body("requestNo",request.path("requestNo").asText(),"versionNo",0,"reason","Adjust demand","items",List.of(Map.of("id",id(line),"formulaItemId",line.path("formulaItemId").asText(),"requestedQty","5","unitId",unit))),token(request),key());assertThat(id(edited.path("items").get(0))).isEqualTo(id(line));
  var command=body("versionNo",1,"reason","Submit demand");String key=key();var submitted=requests.submit(id(edited),command,token(edited),key);assertThat(requests.submit(id(edited),command,token(edited),key)).isEqualTo(submitted);assertThat(submitted.path("status").asText()).isEqualTo("SUBMITTED");
  rejected(IllegalArgumentException.class,()->requests.submit(id(edited),body("versionNo",2,"reason","Unknown state field","status","FULFILLED"),token(submitted),key()));
  rejected(RuntimeException.class,()->requests.cancel(id(submitted),body("versionNo",0,"reason","Stale"),null,key()));var cancelled=requests.cancel(id(submitted),body("versionNo",2,"reason","Unused demand"),token(submitted),key());assertThat(cancelled.path("status").asText()).isEqualTo("CANCELLED");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_reservation WHERE main_batch_id=?",Long.class,id(batch))).isZero();
 }
 @Test void managementPartialFullAndOverIssueAreAtomicAndReturnDoesNotReopen(){
  var batch=releasedBatch();releaseIncoming(()->{});as(author);var request=submit(demand(batch,"6"));reserve(batch,request,"10");var before=onHand();
  var first=linkedIssue(batch,request,"4");stock.confirmIssue(id(first),body("reason","First partial"),token(first),key());var partial=requests.get(id(request));assertThat(partial.path("status").asText()).isEqualTo("PARTIALLY_ISSUED");assertThat(new java.math.BigDecimal(partial.path("items").get(0).path("remainingQty").asText())).isEqualByComparingTo("2");
  var excessive=linkedIssue(batch,partial,"3");blockedCode("REQUEST_QTY_EXCEEDED",()->stock.confirmIssue(id(excessive),body("reason","Must reject excess"),token(excessive),key()));assertThat(wms.get("MaterialIssue",id(excessive)).path("status").asText()).isEqualTo("DRAFT");assertThat(requests.get(id(request)).path("items").get(0).path("issuedQty").asText()).isEqualTo("4.000000");
  var last=linkedIssue(batch,partial,"2");var confirmed=stock.confirmIssue(id(last),body("reason","Fulfill demand"),token(last),key());assertThat(requests.get(id(request)).path("status").asText()).isEqualTo("FULFILLED");assertThat(onHand()).isEqualByComparingTo(before);
  stock.returnIssue(id(confirmed),body("reason","Return unused handover","items",List.of(Map.of("issueItemId",id(confirmed.path("items").get(0)),"quantity","1","unitId",unit))),token(confirmed),key());assertThat(requests.get(id(request)).path("status").asText()).isEqualTo("FULFILLED");assertThat(onHand()).isEqualByComparingTo(before);
  var history=management.returns(0,20,Map.of("issueId",id(confirmed)));assertThat(history.total()).isEqualTo(1);assertThat(history.items().getFirst().path("remainingReturnQty").asText()).isEqualTo("1.000000");
 }
 @Test void managementLotInventoryAndReferenceFiltersDoNotInventLocationReservations(){
  var batch=releasedBatch();releaseIncoming(()->{});as(author);var request=submit(demand(batch,"3"));reserve(batch,request,"3");var rows=management.inventory(0,20,Map.of("materialLotId",lot));assertThat(rows.total()).isEqualTo(1);var row=rows.items().getFirst();assertThat(row.path("reservedQty").asText()).isEqualTo("3.000000");assertThat(new java.math.BigDecimal(row.path("availableQty").asText())).isEqualByComparingTo(onHand().subtract(new java.math.BigDecimal("3")));assertThat(row.path("locationBalances").get(0).has("reservedQty")).isFalse();assertThat(management.inventory(0,20,Map.of("warehouseId","9223372036854775806")).items()).isEmpty();
  assertThat(requests.list(0,20,Map.of("mainBatchId",id(batch))).total()).isEqualTo(1);rejected(IllegalArgumentException.class,()->management.inventory(0,20,Map.of("inventoryStatus","FAKE")));rejected(IllegalArgumentException.class,()->requests.list(0,20,Map.of("sort","danger")));
 }
 @Test void managementPermissionAndLinkedCancellationAreServerControlled(){
  var batch=releasedBatch();releaseIncoming(()->{});as(author);var request=submit(demand(batch,"2"));var issue=linkedIssue(batch,request,"2");blockedCode("REQUEST_HAS_ISSUES",()->requests.cancel(id(request),body("versionNo",1,"reason","Cannot cancel linked"),token(request),key()));permissions.remove("wms:request:submit");as(author);assertThat(requests.get(id(request)).path("allowedActions").toString()).doesNotContain("SUBMIT");permissions.remove("wms:request:view");as(author);rejected(RuntimeException.class,()->requests.get(id(request)));assertThat(wms.get("MaterialIssue",id(issue)).path("materialRequestNo").asText()).isEqualTo(request.path("requestNo").asText());
 }
 @Test void managementHttpContractAndOrganizationScope() throws Exception {
  var batch=releasedBatch();var request=demand(batch,"2");var reader=org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("wms-http").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("wms:request:view"));
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/material-requests").param("mainBatchId",id(batch)).with(reader)).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.total").value(1));
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/material-requests").param("createdFrom","not-a-date").with(reader)).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/material-requests/"+id(request)).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("wrong"))).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
  org.mockito.Mockito.when(contexts.current()).thenReturn(new com.hospital.mes.audit.application.CurrentPlatformContext(2,author,Set.of("INCOMING_TEST"),permissions,"incoming-session","wrong-org"));
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/material-requests/"+id(request)).with(reader)).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());as(author);
 }
 @Test void managementMixedUnitsKeepGrossDemandAndOriginalReturnUnit(){
  var batch=releasedBatch();releaseIncoming(()->{});as(author);
  var alternate=requestUnits.create(new com.hospital.mes.masterdata.application.UnitCommands.Create("ALT"+suffix,"Test alternate mass unit","MASS",6),key());
  String alt=id(alternate);
  conversions.create(new com.hospital.mes.masterdata.application.UnitConversionCommands.Create(alt,unit,"0.001",material),key());
  conversions.create(new com.hospital.mes.masterdata.application.UnitConversionCommands.Create(unit,alt,"1000",material),key());
  var request=submit(demand(batch,"1"));reserve(batch,request,"1");var line=request.path("items").get(0);
  var issue=stock.createIssue(body("issueNo","MIX"+suffix,"mainBatchId",id(batch),"materialRequestId",id(request),"reason","Mixed unit issue","items",List.of(Map.of("materialLotId",lot,"formulaItemId",line.path("formulaItemId").asText(),"materialRequestItemId",id(line),"issuedQty","1000","unitId",alt))),key());
  issue=stock.confirmIssue(id(issue),body("reason","Confirm mixed unit"),token(issue),key());
  assertThat(new java.math.BigDecimal(requests.get(id(request)).path("items").get(0).path("issuedQty").asText())).isEqualByComparingTo("1");
  stock.returnIssue(id(issue),body("reason","Return in base unit","items",List.of(Map.of("issueItemId",id(issue.path("items").get(0)),"quantity","0.25","unitId",unit))),token(issue),key());
  var row=management.returns(0,20,Map.of("issueId",id(issue))).items().getFirst();
  assertThat(new java.math.BigDecimal(row.path("returnedQty").asText())).isEqualByComparingTo("250");
  assertThat(new java.math.BigDecimal(row.path("remainingReturnQty").asText())).isEqualByComparingTo("750");
  assertThat(row.path("issuedUnitId").asText()).isEqualTo(alt);assertThat(row.path("unitId").asText()).isEqualTo(unit);assertThat(requests.get(id(request)).path("status").asText()).isEqualTo("FULFILLED");
 }
 @Test void managementLinkedDemandThroughSignedProductionChargeKeepsStockAndTrace(){
  var batch=releasedBatch();var request=submit(demand(batch,"3"));String initialBatchId=id(batch),initialVersion=token(batch);
  assertReservationBlocked(initialBatchId,request.path("items").get(0).path("formulaItemId").asText(),initialVersion);
  incomingReportId=id(releaseIncoming(()->assertReservationBlocked(initialBatchId,request.path("items").get(0).path("formulaItemId").asText(),initialVersion)));as(author);reserve(batch,request,"3");
  var before=onHand();var issue=linkedIssue(batch,request,"3");issue=stock.confirmIssue(id(issue),body("reason","Actual warehouse handover"),token(issue),key());assertThat(onHand()).isEqualByComparingTo(before);
  batch=production.startBatch(id(batch),body("reason","Start actual production"),token(batch),key());String executionId=batch.path("executionUnits").get(0).path("id").asText();
  var op=executionService.operations(executionId).getFirst();op=executionService.start(id(op),body("reason","Start operation"),token(op),key());
  var scale=equipment.create(new com.hospital.mes.equipment.application.EquipmentCommands.Create("RQSC"+suffix,"Request scale","PRODUCTION_IT_SCALE",java.time.LocalDate.now(java.time.ZoneOffset.UTC).plusDays(30),"Production"),key());var execution=production.execution(executionId);
  var weighed=weighing.createWeighing(body("executionUnitId",executionId,"materialLotId",lot,"formulaItemId",request.path("items").get(0).path("formulaItemId").asText(),"targetQty","2","actualQty","2","unitId",unit,"scaleEquipmentId",id(scale),"versionNo",execution.path("versionNo").asLong(),"reason","Actual weighing after handover"),key());
  as(reviewer);var verified=weighing.verifyWeighing(id(weighed),body("reason","Independent signed verification","reauthToken","token"),token(weighed),key());assertThat(verifier.verify(1,verified.path("signatureEvidence").get(0).path("signatureId").asLong())).isTrue();
  as(author);var charge=weighing.createCharge(body("executionUnitId",executionId,"operationExecutionId",id(op),"materialLotId",lot,"weighingRecordId",id(weighed),"chargedQty","2","unitId",unit,"versionNo",op.path("versionNo").asLong(),"reason","Actual production charge"),key());
  stock.returnIssue(id(issue),body("reason","Return unused issue","items",List.of(Map.of("issueItemId",id(issue.path("items").get(0)),"quantity","1","unitId",unit))),token(issue),key());
  assertThat(onHand()).isEqualByComparingTo(before.subtract(new java.math.BigDecimal("2")));assertThat(requests.get(id(request)).path("status").asText()).isEqualTo("FULFILLED");assertThat(trace.trace(null,lot).toString()).contains(id(charge),incomingReportId);assertThat(requests.get(id(request)).path("linkedIssues").get(0).path("id").asText()).isEqualTo(id(issue));assertThat(requests.get(id(request)).path("linkedIssues").get(0).path("returns")).hasSize(1);
  assertThat(new java.math.BigDecimal(management.inventory(0,20,Map.of("materialLotId",lot)).items().getFirst().path("reservedQty").asText())).isEqualByComparingTo("1");
 }
 @Test void managementRetainedLinesAndInvalidLinkedIdentitiesFailClosed(){
  var batch=releasedBatch(true);releaseIncoming(()->{});as(author);var formula=batch.path("processSnapshot").path("snapshot").path("process").path("formula").path("items");
  var input=body("requestNo","RQ_TWO_"+suffix,"mainBatchId",id(batch),"reason","Two distinct frozen formulas","items",List.of(Map.of("formulaItemId",formula.get(0).path("formulaItemId").asText(),"requestedQty","2","unitId",unit),Map.of("formulaItemId",formula.get(1).path("formulaItemId").asText(),"requestedQty","1","unitId",unit)));
  String createKey=key();var request=requests.create(input,createKey);assertThat(requests.create(input,createKey)).isEqualTo(request);
  var changed=input.deepCopy();changed.put("reason","Changed replay");rejected(RuntimeException.class,()->requests.create(changed,createKey));rejected(RuntimeException.class,()->requests.create(input,key()));
  var duplicate=input.deepCopy();duplicate.put("requestNo","RQ_DUP_"+suffix);duplicate.withArray("items").set(1,duplicate.path("items").get(0));blockedCode("DUPLICATE_REQUEST_FORMULA",()->requests.create(duplicate,key()));
  for(String bad:List.of("0","-1","0.0000001","1000000000000")){var invalid=input.deepCopy();invalid.put("requestNo","BAD_"+UUID.randomUUID());((com.fasterxml.jackson.databind.node.ObjectNode)invalid.path("items").get(0)).put("requestedQty",bad);rejected(RuntimeException.class,()->requests.create(invalid,key()));}
  var line=request.path("items").get(0);blockedCode("REQUEST_ITEM_REMOVAL_FORBIDDEN",()->requests.edit(id(request),body("requestNo",request.path("requestNo").asText(),"versionNo",0,"reason","Cannot remove history","items",List.of(Map.of("id",id(line),"formulaItemId",line.path("formulaItemId").asText(),"requestedQty","2","unitId",unit))),token(request),key()));
  var submitted=submit(request);var other=submit(demand(batch,"2"));var issue=linkedIssue(batch,submitted,"2");
  var switched=body("mainBatchId",id(batch),"materialRequestId",id(other),"issueNo",issue.path("issueNo").asText(),"versionNo",0,"reason","Cannot reassign request","items",List.of(Map.of("materialLotId",lot,"formulaItemId",line.path("formulaItemId").asText(),"materialRequestItemId",id(other.path("items").get(0)),"issuedQty","2","unitId",unit)));
  rejected(RuntimeException.class,()->stock.updateIssue(id(issue),switched,token(issue),key()));
  var foreign=switched.deepCopy();foreign.remove("versionNo");foreign.put("issueNo","FOREIGN_"+suffix);foreign.put("materialRequestId",id(submitted));rejected(RuntimeException.class,()->stock.createIssue(foreign,key()));
  assertThat(requests.get(id(request)).path("items")).hasSize(2);assertThat(wms.get("MaterialIssue",id(issue)).path("materialRequestId").asText()).isEqualTo(id(request));
  for(String action:List.of("view","create","update","submit","cancel")){permissions.remove("wms:request:"+action);as(author);switch(action){case "view"->rejected(RuntimeException.class,()->requests.get(id(request)));case "create"->rejected(RuntimeException.class,()->requests.create(input,key()));case "update"->rejected(RuntimeException.class,()->requests.edit(id(request),body("versionNo",1,"reason","Permission check"),null,key()));case "submit"->rejected(RuntimeException.class,()->requests.submit(id(request),body("versionNo",1,"reason","Permission check"),null,key()));case "cancel"->rejected(RuntimeException.class,()->requests.cancel(id(request),body("versionNo",1,"reason","Permission check"),null,key()));}permissions.add("wms:request:"+action);as(author);}
 }
 @Test void managementMultiLocationAndMultiBatchTotalsRemainWholeLot(){
  var quarantine=management.inventory(0,20,Map.of("materialLotId",lot)).items().getFirst();assertThat(quarantine.path("qualityStatus").asText()).isEqualTo("QUARANTINE");assertThat(new java.math.BigDecimal(quarantine.path("availableQty").asText())).isPositive();
  var firstBatch=releasedBatch();releaseIncoming(()->{});as(author);var first=submit(demand(firstBatch,"3"));reserve(firstBatch,first,"3");
  suffix=UUID.randomUUID().toString().replace("-", "").substring(0,12);var secondBatch=releasedBatch();var second=submit(demand(secondBatch,"2"));reserve(secondBatch,second,"2");
  String source=jdbc.queryForObject("SELECT location_id FROM wms_inventory_ledger WHERE material_lot_id=? ORDER BY id LIMIT 1",String.class,lot);String warehouse=jdbc.queryForObject("SELECT warehouse_id FROM wms_location WHERE id=?",String.class,source);
  var destination=wms.maintenance("Location",null,new com.hospital.mes.wms.domain.WmsCommands.LocationCreate(warehouse,"MOVE_"+suffix,"Second test-owned location"),null,key());var lotFact=wms.get("MaterialLot",lot);permissions.add("wms:inventory:move");as(author);
  wms.move(new com.hospital.mes.wms.domain.WmsCommands.InventoryMove(lot,source,null,id(destination),null,"4",unit,lotFact.path("versionNo").asLong(),"Actual multi-location fixture movement"),token(lotFact),key());
  var all=management.inventory(0,1,Map.of("materialLotId",lot));var filtered=management.inventory(0,1,Map.of("materialLotId",lot,"locationId",id(destination)));assertThat(all.total()).isEqualTo(1);assertThat(filtered.items()).isEqualTo(all.items());var row=all.items().getFirst();assertThat(row.path("locationBalances")).hasSize(2);assertThat(new java.math.BigDecimal(row.path("onHandQty").asText())).isEqualByComparingTo("10");assertThat(new java.math.BigDecimal(row.path("reservedQty").asText())).isEqualByComparingTo("5");assertThat(new java.math.BigDecimal(row.path("availableQty").asText())).isEqualByComparingTo("5");assertThat(management.inventory(1,1,Map.of("materialLotId",lot)).items()).isEmpty();
  jdbc.update("UPDATE wms_reservation SET reserved_qty=20 WHERE main_batch_id=? AND material_lot_id=?",id(secondBatch),lot);
  // Synthetic corruption is outside MyBatis commands; discard its test-transaction cache.
  requestSql.clearCache();blockedCode("INVENTORY_INTEGRITY",()->management.inventory(0,20,Map.of("materialLotId",lot)));
 }
 @Test void managementZeroBalanceCannotHideOutstandingReservationIntegrity(){
  var batch=releasedBatch();releaseIncoming(()->{});as(author);var request=submit(demand(batch,"3"));reserve(batch,request,"3");
  String location=jdbc.queryForObject("SELECT location_id FROM wms_inventory_ledger WHERE material_lot_id=? ORDER BY id LIMIT 1",String.class,lot);var lotFact=wms.get("MaterialLot",lot);permissions.add("wms:inventory:adjust");as(author);
  wms.adjust(new com.hospital.mes.wms.domain.WmsCommands.InventoryAdjust(lot,location,null,"-10",unit,lotFact.path("versionNo").asLong(),"Test-owned zero balance integrity fixture"),token(lotFact),key());
  blockedCode("INVENTORY_INTEGRITY",()->management.inventory(0,20,Map.of("materialLotId",lot)));
  jdbc.update("UPDATE wms_reservation SET status='RELEASED' WHERE main_batch_id=? AND material_lot_id=?",id(batch),lot);requestSql.clearCache();
  var zero=management.inventory(0,20,Map.of("materialLotId",lot));assertThat(zero.total()).isEqualTo(1);assertThat(new java.math.BigDecimal(zero.items().getFirst().path("onHandQty").asText())).isZero();assertThat(management.inventory(0,20,Map.of("materialLotId",lot,"locationId",location)).items()).isEmpty();
 }
}
