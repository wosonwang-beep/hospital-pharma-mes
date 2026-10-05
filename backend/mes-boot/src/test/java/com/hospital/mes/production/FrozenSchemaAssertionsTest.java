package com.hospital.mes.production;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class FrozenSchemaAssertionsTest {
 @Test void frozenNullableUnionAllowsExistingNullAndRejectsWrongNonNullTypes(){
  var json=new ObjectMapper();
  assertThatCode(()->FrozenSchemaAssertions.assertProperty(json,"MainBatch","plannedDate",NullNode.instance)).doesNotThrowAnyException();
  assertThatCode(()->FrozenSchemaAssertions.assertProperty(json,"MainBatch","plannedDate",TextNode.valueOf("2026-10-05"))).doesNotThrowAnyException();
  assertThatThrownBy(()->FrozenSchemaAssertions.assertProperty(json,"MainBatch","plannedDate",IntNode.valueOf(3))).isInstanceOf(AssertionError.class).hasMessageContaining("type union mismatch");
  assertThatCode(()->FrozenSchemaAssertions.assertProperty(json,"ExecutionUnit","subBatchId",NullNode.instance)).doesNotThrowAnyException();
  assertThatThrownBy(()->FrozenSchemaAssertions.assertProperty(json,"ExecutionUnit","subBatchId",BooleanNode.TRUE)).isInstanceOf(AssertionError.class);
 }
}
