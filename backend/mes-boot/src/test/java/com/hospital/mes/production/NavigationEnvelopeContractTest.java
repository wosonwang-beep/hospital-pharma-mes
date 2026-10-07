package com.hospital.mes.production;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.masterdata.application.ScopedStore;
import com.hospital.mes.production.api.NavigationController;
import com.hospital.mes.production.application.NavigationQueryService;
import com.hospital.mes.qms.api.FinishedTestReadController;
import com.hospital.mes.qms.application.FinishedTestReadService;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
/** Controller boundary envelope and closed detail query; actual rows checked by native IT. */
class NavigationEnvelopeContractTest {
 private final ObjectMapper json=new ObjectMapper();
 @Test void existingResponseEnvelopeMatchesEachNewClosedProjection(){
  var service=mock(NavigationQueryService.class);var controller=new NavigationController(service,()->"entry-contract");
  var empty=new ScopedStore.PageData<com.fasterxml.jackson.databind.JsonNode>(List.of(),0,0,20);
  when(service.executions(0,20,Map.of())).thenReturn(empty);when(service.batches("balance",0,20,Map.of())).thenReturn(empty);
  FrozenSchemaAssertions.assertSchema(json,"1.0.23","ApiExecutionEntryPage",json.valueToTree(controller.executions(0,20,Map.of())));
  FrozenSchemaAssertions.assertSchema(json,"1.0.23","ApiBatchEntryPage",json.valueToTree(controller.batches("balance",0,20,Map.of())));
  var tests=mock(FinishedTestReadService.class);when(tests.list(0,20,Map.of())).thenReturn(empty);
  FrozenSchemaAssertions.assertSchema(json,"1.0.23","ApiFinishedTestEntryPage",json.valueToTree(new FinishedTestReadController(tests,()->"entry-contract").list(0,20,Map.of())));
 }
 @Test void unknownDetailQueryCannotReachAReadService(){
  var tests=mock(FinishedTestReadService.class);var controller=new FinishedTestReadController(tests,()->"entry-contract");
  assertThatThrownBy(()->controller.get("1",Map.of("other","x"))).isInstanceOf(IllegalArgumentException.class);verifyNoInteractions(tests);
  var batches=mock(NavigationQueryService.class);var navigation=new NavigationController(batches,()->"entry-contract");
  assertThatThrownBy(()->navigation.batch("1","balance",Map.of("context","balance","other","x"))).isInstanceOf(IllegalArgumentException.class);verifyNoInteractions(batches);
 }
}
