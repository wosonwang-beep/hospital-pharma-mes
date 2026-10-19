package com.hospital.mes.masterdata.domain;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SupplierQualificationRulesTest {

    @Test void supplierQualificationHasExactlyThreeChineseValues() {
        assertThat(SupplierRules.PENDING).isEqualTo("待评审");
        assertThat(SupplierRules.QUALIFIED).isEqualTo("合格");
        assertThat(SupplierRules.UNQUALIFIED).isEqualTo("不合格");
        assertThat(SupplierRules.qualify("待评审", LocalDate.parse("2028-12-31"), true,
                Instant.parse("2026-10-11T00:00:00Z"))).isEqualTo("合格");
        assertThat(SupplierRules.reject("待评审", true)).isEqualTo("不合格");
        assertThat(SupplierRules.reject("合格", true)).isEqualTo("不合格");
        assertThat(SupplierRules.review("不合格", true)).isEqualTo("待评审");
        assertThat(SupplierRules.review("合格", true)).isEqualTo("待评审");
    }

    @Test void unsupportedStatusAndPrematureQualificationFailClosed() {
        var at = Instant.parse("2026-10-11T00:00:00Z");
        for (var invalid : new String[] {"APPROVED", "UNAPPROVED", "INACTIVE", "不合格", "合格"}) {
            assertThatThrownBy(() -> SupplierRules.qualify(invalid, null, true, at))
                    .isInstanceOf(com.hospital.mes.common.exception.StateTransitionException.class)
                    .satisfies(error -> assertThat(((com.hospital.mes.common.exception.MesException) error).code()).isEqualTo("INVALID_STATE"));
        }
        assertThatThrownBy(() -> SupplierRules.qualify("待评审", null, false, at))
                .isInstanceOf(com.hospital.mes.common.exception.StateTransitionException.class)
                    .satisfies(error -> assertThat(((com.hospital.mes.common.exception.MesException) error).code()).isEqualTo("INVALID_STATE"));
        assertThatThrownBy(() -> SupplierRules.qualify("待评审", LocalDate.parse("2026-10-10"), true, at))
                .hasMessageContaining("SUPPLIER_NOT_APPROVED");
    }

    @Test void onlyEnabledQualifiedAndUnexpiredRelationshipCanSupply() {
        var at = Instant.parse("2026-10-11T00:00:00Z");
        SupplierRules.usable("合格", true, LocalDate.parse("2026-10-11"), true,
                LocalDate.parse("2026-10-11"), at);
        for (var state : new String[] {"待评审", "不合格", "APPROVED"}) {
            assertThatThrownBy(() -> SupplierRules.usable(state, true, null, true, null, at))
                    .hasMessageContaining("SUPPLIER_NOT_APPROVED");
        }
        assertThatThrownBy(() -> SupplierRules.usable("合格", false, null, true, null, at))
                .hasMessageContaining("SUPPLIER_NOT_APPROVED");
        assertThatThrownBy(() -> SupplierRules.usable("合格", true, null, false, null, at))
                .hasMessageContaining("SUPPLIER_NOT_APPROVED");
    }
}
