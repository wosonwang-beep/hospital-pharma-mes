package com.hospital.mes.production;

import com.fasterxml.jackson.databind.*;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.production.application.ProductionQueryService;
import com.hospital.mes.execution.application.ExecutionQueryService;
import com.hospital.mes.traceability.application.*;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraceChargeProjectionTest {
 @SuppressWarnings("unchecked") @Test void unweighedChargesReachEachActualFinishedBatchOnce() throws Exception {
  var json=new ObjectMapper();var mutations=mock(MasterMutation.class);var production=mock(ProductionQueryService.class);var execution=mock(ExecutionQueryService.class);
  ObjectProvider<IncomingTracePort> incoming=mock(ObjectProvider.class);ObjectProvider<FinishedTracePort> finished=mock(ObjectProvider.class);ObjectProvider<WmsTracePort> wms=mock(ObjectProvider.class);var fp=mock(FinishedTracePort.class);
  when(mutations.context("trace:view")).thenReturn(new CurrentPlatformContext(1,1,Set.of(),Set.of("trace:view"),"test","test"));when(finished.getIfAvailable()).thenReturn(fp);
  var empty=json.createObjectNode();empty.putArray("nodes");empty.putArray("edges");when(incoming.getIfAvailable()).thenReturn((org,lot)->empty);
  var charges=new ArrayList<JsonNode>();for(int id=1;id<=3;id++){var charge=json.createObjectNode();charge.put("id",id+"");charge.put("executionUnitId",id==3?"11":"10");charge.put("operationExecutionId",id==3?"21":"20");charge.put("materialLotId","50");charge.put("status","CONFIRMED");charges.add(charge);}
  when(execution.chargesForLot(1,50)).thenReturn(charges);
  when(production.execution(1,10)).thenReturn(new ProductionQueryService.ExecutionContext(100,10,null,"QA_RELEASED","COMPLETED",json.createObjectNode(),0));
  when(production.execution(1,11)).thenReturn(new ProductionQueryService.ExecutionContext(101,11,null,"QA_RELEASED","COMPLETED",json.createObjectNode(),0));
  when(execution.genealogyForBatch(1,100)).thenReturn(List.of(json.readTree("{\"chargeId\":\"1\",\"inputMaterialLotId\":\"50\"}"),json.readTree("{\"chargeId\":\"2\",\"inputMaterialLotId\":\"50\"}")));
  when(execution.genealogyForBatch(1,101)).thenReturn(List.of(json.readTree("{\"chargeId\":\"3\",\"inputMaterialLotId\":\"50\"}")));
  when(execution.operationFact(eq(1L),anyLong())).thenReturn(json.readTree("{\"status\":\"COMPLETED\"}"));
  when(fp.lotForBatch(1,100)).thenReturn(60L);when(fp.lotForBatch(1,101)).thenReturn(61L);
  when(fp.forLot(1,60)).thenReturn(json.readTree("{\"mainBatchId\":\"100\",\"lot\":{\"id\":\"60\"},\"shipments\":[{\"id\":\"90\"}]}"));
  when(fp.forLot(1,61)).thenReturn(json.readTree("{\"mainBatchId\":\"101\",\"lot\":{\"id\":\"61\"},\"shipments\":[{\"id\":\"91\"}]}"));
  var service=new TraceService(mutations,production,execution,incoming,json,finished,wms);var graph=service.trace(null,"50");
  assertThat(graph.path("nodes")).filteredOn(n->n.path("type").asText().equals("CHARGE")).hasSize(3);
  assertThat(graph.path("nodes")).filteredOn(n->n.path("type").asText().equals("FINISHED_SHIPMENT")).hasSize(2);
  assertThat(graph.path("nodes")).noneSatisfy(n->assertThat(n.path("type").asText()).isEqualTo("WEIGHING"));
  verify(fp,times(1)).lotForBatch(1,100);verify(fp,times(1)).lotForBatch(1,101);verify(execution,never()).weighing(anyLong(),anyLong());
 }
}
