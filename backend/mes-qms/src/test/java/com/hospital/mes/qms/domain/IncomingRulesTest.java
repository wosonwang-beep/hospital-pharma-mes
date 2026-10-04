package com.hospital.mes.qms.domain;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class IncomingRulesTest {
 @Test void numericBoundariesAreInclusiveAndMissingNeverPasses(){
  assertEquals("PASS",IncomingRules.numeric(new BigDecimal("1"),new BigDecimal("1"),new BigDecimal("2")));
  assertEquals("FAIL",IncomingRules.numeric(new BigDecimal("2.000001"),new BigDecimal("1"),new BigDecimal("2")));
  assertThrows(RuntimeException.class,()->IncomingRules.numeric(null,BigDecimal.ZERO,BigDecimal.ONE));
 }
 @Test void failCannotBeCorrectedToPassOrInvalid(){
  assertThrows(RuntimeException.class,()->IncomingRules.correction("FAIL","PASS"));
  assertThrows(RuntimeException.class,()->IncomingRules.correction("FAIL","INVALID"));
  assertDoesNotThrow(()->IncomingRules.correction("FAIL","FAIL"));
 }
 @Test void originalValidFailBlocksEvenAfterPassingRetest(){
  assertFalse(IncomingRules.resolvedFail("CLOSED","VALID","PASS",true));
  assertFalse(IncomingRules.resolvedFail("DECIDED","INVALID","PASS",true));
  assertFalse(IncomingRules.resolvedFail("CLOSED","INVALID","PASS",false));
  assertTrue(IncomingRules.resolvedFail("CLOSED","INVALID","PASS",true));
 }
 @Test void numericPrecisionCannotBeSilentlyRounded(){
  assertThrows(RuntimeException.class,()->IncomingRules.quantity("0"));
  assertThrows(RuntimeException.class,()->IncomingRules.quantity("1.000000001"));
  assertEquals(new BigDecimal("1.25000000"),IncomingRules.quantity("1.25000000"));
 }
}
