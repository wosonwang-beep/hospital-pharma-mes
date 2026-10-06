package com.hospital.mes.production;

import static org.assertj.core.api.Assertions.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.wms.application.MaterialRequestService;
import com.hospital.mes.wms.domain.WmsCommands;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

/** Unique retained native fixture is QA-blocked by the existing fixture after the race. */
class WmsRequestConcurrencyIT extends NativeConcurrencyIT {
 @Autowired MaterialRequestService requests;
 @BeforeEach void requestPermissions(){for(String a:List.of("view","create","submit","cancel"))permissions.add("wms:request:"+a);permissions.add("wms:inventory:adjust");as(author);}
 JsonNode issueFor(JsonNode request){var line=request.path("items").get(0);return stock.createIssue(body("issueNo","RQ_RACE_"+UUID.randomUUID(),"mainBatchId",batchId,"materialRequestId",id(request),"reason","Approved retained request concurrency fixture","items",List.of(Map.of("formulaItemId",line.path("formulaItemId").asText(),"materialRequestItemId",id(line),"materialLotId",lot,"issuedQty","6","unitId",unit))),key());}
 @Test void concurrentConfirmationsCannotExceedDemand() throws Exception {
  batch=releasedBatch();batchId=id(batch);releaseIncoming(()->{});as(author);
  String location=jdbc.queryForObject("SELECT location_id FROM wms_inventory_ledger WHERE material_lot_id=? ORDER BY id LIMIT 1",String.class,lot);
  var lotFact=wms.get("MaterialLot",lot);
  wms.adjust(new WmsCommands.InventoryAdjust(lot,location,null,"5",unit,lotFact.path("versionNo").asLong(),"Unique retained concurrency fixture stock"),token(lotFact),key());
  String formula=batch.path("processSnapshot").path("snapshot").path("process").path("formula").path("items").get(0).path("formulaItemId").asText();
  var request=requests.create(body("requestNo","RQ_RACE_"+suffix,"mainBatchId",batchId,"reason","Concurrent demand","items",List.of(Map.of("formulaItemId",formula,"requestedQty","10","unitId",unit))),key());
  request=requests.submit(id(request),body("versionNo",0,"reason","Submit retained demand"),token(request),key());
  stock.reserve(batchId,body("reason","Existing stock reservation","items",List.of(Map.of("formulaItemId",formula,"materialLotId",lot,"reservedQty","15","unitId",unit))),token(batch),key());
  var first=issueFor(request);var second=issueFor(request);String requestId=id(request);var actor=contexts.current();commitFixture();
  var result=race("SELECT id FROM prd_production_order WHERE id=? FOR UPDATE",orderId,actor,()->stock.confirmIssue(id(first),body("reason","First actual concurrent confirmation"),token(first),key()),actor,()->stock.confirmIssue(id(second),body("reason","Second actual concurrent confirmation"),token(second),key()));
  assertThat(result.firstError()).isNull();assertThat(result.first().path("status").asText()).isEqualTo("CONFIRMED");assertThat(result.secondError()).isNotNull();
  as(author);var current=requests.get(requestId);assertThat(current.path("status").asText()).isEqualTo("PARTIALLY_ISSUED");assertThat(current.path("items").get(0).path("issuedQty").asText()).isEqualTo("6.000000");assertThat(current.path("items").get(0).path("remainingQty").asText()).isEqualTo("4.000000");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_material_issue WHERE material_request_id=? AND status='CONFIRMED'",Long.class,requestId)).isEqualTo(1);assertThat(jdbc.queryForObject("SELECT status FROM wms_material_issue WHERE id=?",String.class,id(second))).isEqualTo("DRAFT");
  blockedCode("REQUEST_QTY_EXCEEDED",()->stock.confirmIssue(id(second),body("reason","Fresh retry must still reject quota"),token(second),key()));
 }
 @Test void concurrentLinkedDraftCreationPreventsCancellation() throws Exception {
  batch=releasedBatch();batchId=id(batch);releaseIncoming(()->{});as(author);
  String formula=batch.path("processSnapshot").path("snapshot").path("process").path("formula").path("items").get(0).path("formulaItemId").asText();
  var request=requests.create(body("requestNo","RQ_CANCEL_RACE_"+suffix,"mainBatchId",batchId,"reason","Retained cancellation concurrency fixture","items",List.of(Map.of("formulaItemId",formula,"requestedQty","10","unitId",unit))),key());
  request=requests.submit(id(request),body("versionNo",0,"reason","Submit demand"),token(request),key());String requestId=id(request),requestVersion=token(request);var actor=contexts.current();var submitted=request;permissions.add("wms:request:cancel");commitFixture();
  var result=race("SELECT id FROM prd_production_order WHERE id=? FOR UPDATE",orderId,actor,()->issueFor(submitted),actor,()->requests.cancel(requestId,body("versionNo",1,"reason","Concurrent cancellation must see newly linked draft"),requestVersion,key()));
  assertThat(result.firstError()).isNull();assertThat(result.secondError()).as("Cancellation must not miss the draft committed while it waited").isNotNull();
  assertThat(result.secondError()).isInstanceOf(com.hospital.mes.common.exception.MesException.class);assertThat(((com.hospital.mes.common.exception.MesException)result.secondError()).code()).isIn("REQUEST_HAS_ISSUES","CONCURRENT_MODIFICATION");
  as(author);assertThat(requests.get(requestId).path("status").asText()).isEqualTo("SUBMITTED");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wms_material_issue WHERE material_request_id=?",Long.class,requestId)).isEqualTo(1);
  blockedCode("REQUEST_HAS_ISSUES",()->requests.cancel(requestId,body("versionNo",1,"reason","Fresh cancellation retry must see linked draft"),requestVersion,key()));
 }
}
