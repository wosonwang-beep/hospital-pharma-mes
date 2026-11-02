package com.hospital.mes.process.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.common.exception.ComplianceException;
import org.junit.jupiter.api.Test;
import java.util.List;
import static com.hospital.mes.process.domain.ProcessCommands.*;
import static org.assertj.core.api.Assertions.*;

class OperationTypeContractTest {
 private final ObjectMapper json = new ObjectMapper();
 private Operation read(String type) throws Exception {
  return json.readValue("{\"operationCode\":\"OP01\",\"operationName\":\"溶液配制\",\"sequenceNo\":1,\"requiredRole\":null,\"completionRule\":null,\"predecessorCodes\":[],\"requiredEquipmentType\":null,\"clearanceRequired\":false,\"parameters\":[],\"ipcDefinitions\":[],\"operationType\":"+type+"}", Operation.class);
 }
 private void validate(Operation op) { ProcessRules.route(new RouteSave(null,null,"KCL30-ROUTE",List.of(op)),true); }
 @Test void explicitProductionAndIpcTypesRoundTripWithoutInventingInspectionRequirements() throws Exception {
  for(String type:List.of("PRODUCTION","IPC")) {
   var op=read("\""+type+"\""); validate(op);
   assertThat(json.valueToTree(op).path("operationType").asText()).isEqualTo(type);
   assertThat(op.ipcDefinitions()).isEmpty();
   assertThat(op.completionRule()).isNull();
  }
 }
 @Test void unknownOrBlankTypeIsRejectedByDomainValidation() {
  for(String value:List.of("\"OTHER\"","\"\"","\"ipc\""))
   assertThatThrownBy(()->validate(read(value))).isInstanceOf(ComplianceException.class).hasMessageContaining("operationType");
 }
 @Test void oldConstructorAndNullTypeKeepLegacySerializedContentUnchanged() throws Exception {
  var old=new Operation("OP01","溶液配制",1,null,null,List.of(),null,false,List.of(),List.of());
  assertThat(json.valueToTree(old).has("operationType")).isFalse();
  com.fasterxml.jackson.databind.JsonNode current=json.valueToTree(read("null"));
  assertThat(current).isEqualTo(json.valueToTree(old));
 }
 @Test void productionWithoutAnyIpcRemainsAValidRoute() throws Exception { validate(read("\"PRODUCTION\"")); }
}
