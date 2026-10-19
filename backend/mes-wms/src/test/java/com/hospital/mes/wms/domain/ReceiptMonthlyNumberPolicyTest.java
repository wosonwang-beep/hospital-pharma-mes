package com.hospital.mes.wms.domain;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.assertj.core.api.Assertions.*;
class ReceiptMonthlyNumberPolicyTest {
 private final ZoneId zone=ZoneId.of("Asia/Shanghai");
 @Test void exactlyTenDigitsNoPrefixOrSeparator(){assertThat(ReceiptNumberingPolicy.format("","yyyyMM",4,1,zone,Instant.parse("2026-10-19T00:00:00Z"))).isEqualTo("2026100001");}
 @Test void monthBoundaryUsesBusinessTimezone(){assertThat(ReceiptNumberingPolicy.format("","yyyyMM",4,1,zone,Instant.parse("2026-10-31T16:00:00Z"))).isEqualTo("2026110001");assertThat(ReceiptNumberingPolicy.period(zone,Instant.parse("2026-10-31T15:59:59Z"))).isEqualTo("202610");}
 @Test void yearBoundaryAndLeapMonthAreValid(){assertThat(ReceiptNumberingPolicy.period(zone,Instant.parse("2026-12-31T16:00:00Z"))).isEqualTo("202701");assertThat(ReceiptNumberingPolicy.format("","yyyyMM",4,23,zone,Instant.parse("2028-02-29T00:00:00Z"))).isEqualTo("2028020023");}
 @Test void fourDigitsNeverGrowOrWrap(){assertThat(ReceiptNumberingPolicy.format("","yyyyMM",4,9999,zone,Instant.EPOCH)).endsWith("9999");assertThatThrownBy(()->ReceiptNumberingPolicy.format("","yyyyMM",4,10000,zone,Instant.EPOCH)).isInstanceOf(IllegalStateException.class);assertThatThrownBy(()->ReceiptNumberingPolicy.format("","yyyyMM",4,0,zone,Instant.EPOCH)).isInstanceOf(IllegalArgumentException.class);}
 @Test void oldApprovedFormatRemainsUnchanged(){assertThat(ReceiptNumberingPolicy.format("RM","yyyyMMdd",8,3,zone,Instant.parse("2026-10-19T00:00:00Z"))).isEqualTo("RM-20261019-00000003");}
 @Test void unapprovedVariantsAreRejected(){for(int width:new int[]{3,5})assertThatThrownBy(()->ReceiptNumberingPolicy.format("","yyyyMM",width,1,zone,Instant.EPOCH)).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->ReceiptNumberingPolicy.format("RM","yyyyMM",4,1,zone,Instant.EPOCH)).isInstanceOf(IllegalArgumentException.class);}
}
