package com.hospital.mes.wms.domain;

import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class MaterialRequestRulesTest {
 @Test void partialAndFullStatesFollowGrossConfirmedQuantities(){
  assertThat(MaterialRequestRules.fulfillment(List.of(new BigDecimal("10")),List.of(new BigDecimal("4")))).isEqualTo("PARTIALLY_ISSUED");
  assertThat(MaterialRequestRules.fulfillment(List.of(new BigDecimal("10")),List.of(new BigDecimal("10")))).isEqualTo("FULFILLED");
  assertThatThrownBy(()->MaterialRequestRules.fulfillment(List.of(new BigDecimal("10")),List.of(new BigDecimal("11")))).hasMessageContaining("REQUEST_QTY_EXCEEDED");
 }
 @Test void cancelledAndSubmittedRecordsCannotBeEdited(){
  MaterialRequestRules.editable("DRAFT");
  assertThatThrownBy(()->MaterialRequestRules.editable("SUBMITTED")).hasMessageContaining("DRAFT");
  assertThatThrownBy(()->MaterialRequestRules.cancellable("SUBMITTED",true)).hasMessageContaining("linked");
  assertThatThrownBy(()->MaterialRequestRules.cancellable("FULFILLED",false)).hasMessageContaining("cancel");
 }
 @Test void incoherentFreeStockIsNotClippedToZero(){
  assertThat(MaterialRequestRules.available(new BigDecimal("5"),new BigDecimal("2"))).isEqualByComparingTo("3");
  assertThatThrownBy(()->MaterialRequestRules.available(new BigDecimal("2"),new BigDecimal("3"))).hasMessageContaining("INVENTORY_INTEGRITY");
 }
}
