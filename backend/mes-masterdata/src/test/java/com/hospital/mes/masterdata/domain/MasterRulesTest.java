package com.hospital.mes.masterdata.domain;

import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MasterRulesTest {
    @Test void tcMd001RejectsDimensionMismatchWithoutMaterialSpecificConversion() {
        assertThatThrownBy(() -> MasterRules.convert(new BigDecimal("1"), "MASS", "VOLUME", null, null, 3))
            .hasMessageContaining("UNIT_DIMENSION_MISMATCH");
    }
    @Test void conversionPreservesOriginalFactorAndUsesHalfEven() {
        var result = MasterRules.convert(new BigDecimal("1.005"), "MASS", "MASS", new BigDecimal("1"), null, 2);
        assertThat(result.originalValue()).isEqualTo("1.005");
        assertThat(result.factor()).isEqualTo("1");
        assertThat(result.convertedValue()).isEqualTo("1.00");
        var special = MasterRules.convert(new BigDecimal("2"), "MASS", "VOLUME", new BigDecimal("1.250"), 42L, 3);
        assertThat(special.convertedValue()).isEqualTo("2.500");
    }
    @Test void tcMd002EnforcesCompleteHierarchy() {
        MasterRules.hierarchy("ENTERPRISE", null, null);
        MasterRules.hierarchy("FACTORY", "ENTERPRISE", "ACTIVE");
        MasterRules.hierarchy("WORKSHOP", "FACTORY", "ACTIVE");
        MasterRules.hierarchy("LINE", "WORKSHOP", "ACTIVE");
        assertThatThrownBy(() -> MasterRules.hierarchy("LINE", "ENTERPRISE", "ACTIVE")).hasMessageContaining("ORG_HIERARCHY_INVALID");
        assertThatThrownBy(() -> MasterRules.hierarchy("FACTORY", "ENTERPRISE", "INACTIVE")).hasMessageContaining("ORG_HIERARCHY_INVALID");
    }
    @Test void tcQual001ChecksInclusiveUtcExecutionDateAndStatus() {
        var at = Instant.parse("2026-10-03T23:59:59Z");
        MasterRules.qualification("ACTIVE", LocalDate.parse("2026-10-03"), LocalDate.parse("2026-10-03"), at);
        assertThatThrownBy(() -> MasterRules.qualification("ACTIVE", null, LocalDate.parse("2026-10-02"), at)).hasMessageContaining("QUALIFICATION_REQUIRED");
        assertThatThrownBy(() -> MasterRules.qualification("INACTIVE", null, null, at)).hasMessageContaining("QUALIFICATION_REQUIRED");
    }
    @Test void rejectsNonpositiveOverflowFactorsAndInvalidPrecision() {
        assertThatThrownBy(() -> MasterRules.factor("0")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MasterRules.factor("1000000000000")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MasterRules.scale(13)).isInstanceOf(IllegalArgumentException.class);
    }
}
