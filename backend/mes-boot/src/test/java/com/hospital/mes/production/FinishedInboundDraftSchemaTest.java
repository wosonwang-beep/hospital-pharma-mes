package com.hospital.mes.production;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/** Closed approved optional command variants, independently checked against frozen OpenAPI. */
class FinishedInboundDraftSchemaTest {
 private final ObjectMapper json=new ObjectMapper();
 @Test void checkedAndUncheckedVariantsMatchOnlyTheirApprovedClosedShape() throws Exception {
  var old=json.readTree("{\"requestNo\":\"FI-1\",\"mainBatchId\":\"1\",\"reason\":\"Actual application\"}");
  FrozenSchemaAssertions.assertSchema(json,"FinishedInboundCreate",old);
  var checked=((com.fasterxml.jackson.databind.node.ObjectNode)old.deepCopy()).put("createInspectionDraft",true).put("inspectionRequestNo","FQ-1");
  FrozenSchemaAssertions.assertSchema(json,"FinishedInboundCreate",checked);
  var unchecked=((com.fasterxml.jackson.databind.node.ObjectNode)old.deepCopy()).put("createInspectionDraft",false);
  FrozenSchemaAssertions.assertSchema(json,"FinishedInboundCreate",unchecked);
  assertThatThrownBy(()->FrozenSchemaAssertions.assertSchema(json,"FinishedInboundCreate",unchecked.deepCopy().put("inspectionRequestNo","FQ-1"))).isInstanceOf(AssertionError.class);
  assertThatThrownBy(()->FrozenSchemaAssertions.assertSchema(json,"FinishedInboundCreate",checked.deepCopy().put("inspectionRequestNo"," "))).isInstanceOf(AssertionError.class);
  assertThatThrownBy(()->FrozenSchemaAssertions.assertSchema(json,"FinishedInboundCreate",checked.deepCopy().put("createInspectionDraft","true"))).isInstanceOf(AssertionError.class);
  assertThatThrownBy(()->FrozenSchemaAssertions.assertSchema(json,"FinishedInboundCreate",checked.deepCopy().put("qualityStatus","RELEASED"))).isInstanceOf(AssertionError.class);
 }
}
