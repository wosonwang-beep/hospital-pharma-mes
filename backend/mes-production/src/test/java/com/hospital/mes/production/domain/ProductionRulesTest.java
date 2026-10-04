package com.hospital.mes.production.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ProductionRulesTest {
 @Test void childAllocationCannotExceedOrder(){
  assertDoesNotThrow(()->ProductionRules.allocation(new BigDecimal("10"),new BigDecimal("10")));
  assertThrows(RuntimeException.class,()->ProductionRules.allocation(new BigDecimal("10"),new BigDecimal("10.000001")));
 }
 @Test void onlyDraftCanBeEdited(){
  assertDoesNotThrow(()->ProductionRules.requireDraft("DRAFT"));
  assertThrows(RuntimeException.class,()->ProductionRules.requireDraft("RELEASED"));
 }
 @Test void weighingToleranceUsesExactDecimalAndInclusiveBoundary(){
  assertDoesNotThrow(()->ProductionRules.weighing(new BigDecimal("100"),new BigDecimal("101"),new BigDecimal("1"),new BigDecimal("0.001")));
  assertThrows(RuntimeException.class,()->ProductionRules.weighing(new BigDecimal("100"),new BigDecimal("101.001"),new BigDecimal("1"),new BigDecimal("0.001")));
  assertThrows(RuntimeException.class,()->ProductionRules.weighing(new BigDecimal("100"),new BigDecimal("100.0001"),new BigDecimal("1"),new BigDecimal("0.001")));
 }
}
