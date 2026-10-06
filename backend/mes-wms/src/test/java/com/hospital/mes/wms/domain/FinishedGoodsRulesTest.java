package com.hospital.mes.wms.domain;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
class FinishedGoodsRulesTest {
 @Test void warehouseAcceptanceNeedsCompletedProductionAndIndependentActor(){
  assertThatThrownBy(()->FinishedGoodsRules.confirmReceipt("IN_PROGRESS","SUBMITTED",1L,2L)).hasMessageContaining("completed");
  assertThatThrownBy(()->FinishedGoodsRules.confirmReceipt("PRODUCTION_COMPLETED","SUBMITTED",1L,1L)).hasMessageContaining("Independent");
  assertThatCode(()->FinishedGoodsRules.confirmReceipt("PRODUCTION_COMPLETED","SUBMITTED",1L,2L)).doesNotThrowAnyException();
 }
 @Test void shipmentCannotUseQuarantinedExpiredOrInsufficientStock(){
  assertThatThrownBy(()->FinishedGoodsRules.ship("QA_RELEASED","QUARANTINE","BLOCKED",false,new BigDecimal("5"),new BigDecimal("1"))).hasMessageContaining("released");
  assertThatThrownBy(()->FinishedGoodsRules.ship("QA_RELEASED","RELEASED","AVAILABLE",true,new BigDecimal("5"),new BigDecimal("1"))).hasMessageContaining("Expired");
  assertThatThrownBy(()->FinishedGoodsRules.ship("QA_RELEASED","RELEASED","AVAILABLE",false,new BigDecimal("1"),new BigDecimal("2"))).hasMessageContaining("stock");
  assertThatCode(()->FinishedGoodsRules.ship("QA_RELEASED","RELEASED","AVAILABLE",false,new BigDecimal("5"),new BigDecimal("5"))).doesNotThrowAnyException();
 }
}
