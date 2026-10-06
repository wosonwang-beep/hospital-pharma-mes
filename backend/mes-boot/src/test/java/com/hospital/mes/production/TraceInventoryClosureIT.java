package com.hospital.mes.production;

import com.hospital.mes.wms.application.WmsManagementQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

/** Actual source facts; all inherited fixtures are transactionally rolled back. */
class TraceInventoryClosureIT extends FinishedGoodsLifecycleIT {
 @Autowired WmsManagementQueryService inventoryProjection;

 @Test void actualChargeForwardTraceReachesFinishedShipmentWithoutInventedAssignment(){
  var pending=completedQaBatch();as(qa);
  finishedQa.decide(releaseCommand(pending,"RELEASED",null),token(pending),key());
  var receipt=finishedGoods.getInbound(jdbc.queryForObject("SELECT id FROM wms_finished_inbound_request WHERE main_batch_id=?",Long.class,id(pending)).toString());
  var shipment=finishedGoods.createShipment(body("shipmentNo","TI_SHIP"+suffix,"mainBatchId",id(pending),"locationId",receipt.path("locationId").asText(),"quantity","4","unitId",unit,"receivingParty","Hospital dispensary","reason","Actual trace closure dispatch"),key());
  shipment=finishedGoods.shipmentAction(id(shipment),"confirm",body("versionNo",0,"reason","Actual authorized shipment","signature",Map.of("reauthToken","token")),token(shipment),key());
  var graph=finishedTrace.trace(null,lot);
  assertThat(graph.path("nodes")).extracting(n->n.path("type").asText()).contains("CHARGE","FINISHED_INBOUND_REQUEST","FINISHED_REPORT","FINISHED_SHIPMENT","MATERIAL_ISSUE_ITEM","SUPPLIER");
  assertThat(graph.path("nodes")).anySatisfy(n->{assertThat(n.path("type").asText()).isEqualTo("FINISHED_SHIPMENT");assertThat(n.path("id").asText()).isEqualTo(jdbc.queryForObject("SELECT id FROM wms_finished_shipment WHERE shipment_no=?",Long.class,"TI_SHIP"+suffix).toString());});
  var endpoints=new java.util.HashSet<String>();graph.path("nodes").forEach(n->endpoints.add(n.path("type").asText()+":"+n.path("id").asText()));
  graph.path("edges").forEach(e->{assertThat(endpoints).contains(e.path("sourceType").asText()+":"+e.path("sourceId").asText(),e.path("targetType").asText()+":"+e.path("targetId").asText());});
  assertThat(endpoints).hasSize(graph.path("nodes").size());
  FrozenSchemaAssertions.assertSchema(json,"TraceGraph",graph);
  assertThat(finishedTrace.trace(null,pending.path("finishedLotId").asText()).path("nodes")).anySatisfy(n->{assertThat(n.path("type").asText()).isEqualTo("MATERIAL_LOT");assertThat(n.path("id").asText()).isEqualTo(lot);});
  permissions.removeAll(java.util.Set.of("wms:issue:view","wms:receipt:view","wms:finished-shipment:view"));as(qa);
  var restricted=finishedTrace.trace(null,lot);assertThat(restricted.path("nodes")).extracting(n->n.path("type").asText()).doesNotContain("MATERIAL_ISSUE","MATERIAL_ISSUE_ITEM","SUPPLIER","FINISHED_SHIPMENT");
  var visible=new java.util.HashSet<String>();restricted.path("nodes").forEach(n->visible.add(n.path("type").asText()+":"+n.path("id").asText()));restricted.path("edges").forEach(e->assertThat(visible).contains(e.path("sourceType").asText()+":"+e.path("sourceId").asText(),e.path("targetType").asText()+":"+e.path("targetId").asText()));
 }

 @Test void finishedInventoryFiltersBeforePagingKeepsExactStockAndScopedProductContext(){
  var completed=completedProductionBatch(()->{});confirmedFinishedReceipt(completed);as(qa);
  var f=Map.of("finishedOnly","true","mainBatchId",id(completed));
  var rows=inventoryProjection.inventory(0,1,f);assertThat(rows.total()).isEqualTo(1);
  var row=rows.items().getFirst();assertThat(row.path("mainBatchId").asText()).isEqualTo(id(completed));assertThat(row.path("productId").asText()).isEqualTo(completed.path("productId").asText());
  assertThat(row.path("productName").asText()).isNotBlank();assertThat(row.path("onHandQty").asText()).isEqualTo("10.000000");assertThat(row.path("qualityStatus").asText()).isEqualTo("QUARANTINE");
  assertThat(inventoryProjection.inventory(1,1,f).items()).isEmpty();
  assertThat(inventoryProjection.inventory(0,1,Map.of("finishedOnly","true","materialLotId",lot)).total()).isZero();
  assertThat(inventoryProjection.inventory(0,1,Map.of("finishedOnly","true","productId","9223372036854775806")).total()).isZero();
  assertThatThrownBy(()->inventoryProjection.inventory(0,1,Map.of("finishedOnly","yes"))).isInstanceOf(IllegalArgumentException.class);
  FrozenSchemaAssertions.assertSchema(json,"WmsInventoryLot",row);
  var raw=inventoryProjection.inventory(0,1,Map.of("materialLotId",lot)).items().getFirst();assertThat(raw.path("productId").isNull()).isTrue();FrozenSchemaAssertions.assertSchema(json,"WmsInventoryLot",raw);
  assertThat(inventoryProjection.inventory(0,1,Map.of("finishedOnly","true","productId",completed.path("productId").asText(),"mainBatchId",id(completed))).total()).isEqualTo(1);
  assertThat(inventoryProjection.inventory(0,1,Map.of("finishedOnly","true","mainBatchId","9223372036854775806")).total()).isZero();
  org.mockito.Mockito.when(contexts.current()).thenReturn(new com.hospital.mes.audit.application.CurrentPlatformContext(2,qa,java.util.Set.of("INCOMING_TEST"),permissions,"incoming-session","other-org"));
  assertThat(inventoryProjection.inventory(0,1,Map.of("finishedOnly","true","productId",completed.path("productId").asText())).total()).isZero();
 }
}
