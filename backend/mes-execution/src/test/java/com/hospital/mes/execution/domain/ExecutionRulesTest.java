package com.hospital.mes.execution.domain;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ExecutionRulesTest {
 @Test void namedTransitionsRejectIllegalCommands(){
  assertEquals("IN_PROGRESS",ExecutionRules.start("READY"));
  assertEquals("PAUSED",ExecutionRules.pause("IN_PROGRESS"));
  assertEquals("IN_PROGRESS",ExecutionRules.resume("PAUSED"));
  assertEquals("COMPLETED",ExecutionRules.complete("IN_PROGRESS"));
  assertThrows(RuntimeException.class,()->ExecutionRules.start("COMPLETED"));
  assertThrows(RuntimeException.class,()->ExecutionRules.complete("READY"));
 }
 @Test void verifierIsIndependent(){assertDoesNotThrow(()->ExecutionRules.independent(1,2,true));assertThrows(RuntimeException.class,()->ExecutionRules.independent(1,1,true));}
}
