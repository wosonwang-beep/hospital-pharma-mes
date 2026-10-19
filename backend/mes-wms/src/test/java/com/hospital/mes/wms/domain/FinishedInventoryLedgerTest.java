package com.hospital.mes.wms.domain;
import com.hospital.mes.wms.infrastructure.FinishedInventoryLedgerEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class FinishedInventoryLedgerTest {
 @Test void receiptAndShipmentHaveSeparateSourcesAndOppositeDirections(){var at=LocalDateTime.of(2026,10,11,0,0);var receipt=FinishedInventoryLedgerEntity.receipt(142,1,3,3,new BigDecimal("1000"),20,"r",at);var shipment=FinishedInventoryLedgerEntity.shipment(142,1,3,3,new BigDecimal("100"),30,"s",at);assertThat(receipt.getMainBatchId()).isEqualTo(142);assertThat(receipt.getProductId()).isEqualTo(1);assertThat(receipt.getInboundRequestId()).isEqualTo(20);assertThat(receipt.getShipmentId()).isNull();assertThat(shipment.getInboundRequestId()).isNull();assertThat(shipment.getShipmentId()).isEqualTo(30);assertThat(receipt.getDeltaQty().add(shipment.getDeltaQty())).isEqualByComparingTo("900");}
 @Test void invalidDirectionAndPrecisionAreRejected(){var at=LocalDateTime.now();assertThatThrownBy(()->FinishedInventoryLedgerEntity.receipt(142,1,3,3,new BigDecimal("-1"),20,"r",at)).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->FinishedInventoryLedgerEntity.shipment(142,1,3,3,new BigDecimal("-1"),30,"s",at)).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->FinishedInventoryLedgerEntity.receipt(142,1,3,3,new BigDecimal("0.0000001"),20,"r",at)).isInstanceOf(ArithmeticException.class);}
 @Test void businessFactsHaveNoPublicMutators(){assertThat(java.util.Arrays.stream(FinishedInventoryLedgerEntity.class.getDeclaredMethods()).map(java.lang.reflect.Method::getName)).noneMatch(name->name.startsWith("set"));}
}
