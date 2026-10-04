package com.hospital.mes.wms.domain;
import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
class WmsRulesTest {
 @Test void precisionIsNeverSilentlyRounded(){assertThat(WmsRules.exact(new BigDecimal("3"),"0.25","0.750")).isEqualByComparingTo("0.75");assertThatThrownBy(()->WmsRules.exact(new BigDecimal("1"),"0.0000001","0.000")).hasMessageContaining("lose");assertThatThrownBy(()->WmsRules.quantity("1.0000001",false)).isInstanceOf(IllegalArgumentException.class);}
 @Test void quarantineCanMoveButFrozenAndDueLotsCannot(){var today=LocalDate.of(2026,10,3);WmsRules.movable("BLOCKED",today.plusDays(1),null,today);assertThatThrownBy(()->WmsRules.movable("FROZEN",null,null,today)).hasMessageContaining("Frozen");assertThatThrownBy(()->WmsRules.movable("AVAILABLE",today,null,today)).hasMessageContaining("Expiry");assertThatThrownBy(()->WmsRules.movable("BLOCKED",null,today,today)).hasMessageContaining("Expiry");}
 @Test void signedAdjustmentsCannotCreateNegativeStock(){WmsRules.balance(new BigDecimal("5"),new BigDecimal("-5"));assertThatThrownBy(()->WmsRules.balance(new BigDecimal("5"),new BigDecimal("-5.000001"))).hasMessageContaining("negative");assertThatThrownBy(()->WmsRules.quantity("0",true)).isInstanceOf(IllegalArgumentException.class);assertThat(WmsRules.quantity("-2",true)).isEqualByComparingTo("-2");}
 @Test void confirmedReceiptIsImmutableAndStaleCommandsConflict(){WmsRules.draft("DRAFT");assertThatThrownBy(()->WmsRules.draft("APPROVED")).hasMessageContaining("draft");assertThatThrownBy(()->WmsRules.version(2,1)).hasMessageContaining("Reload");}
 @Test void receiptRequiresExplicitPassedChecks(){assertThatThrownBy(()->WmsRules.check(null)).isInstanceOf(IllegalArgumentException.class);WmsRules.check(false);assertThatThrownBy(()->WmsRules.passed(false)).hasMessageContaining("Every receipt");WmsRules.passed(true);}
 @Test void chronologicalDatesAreRequired(){var today=LocalDate.of(2026,10,3);assertThatThrownBy(()->WmsRules.dates(today.plusDays(1),null,null,today)).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->WmsRules.dates(today,today.minusDays(1),null,today)).isInstanceOf(IllegalArgumentException.class);}
}
