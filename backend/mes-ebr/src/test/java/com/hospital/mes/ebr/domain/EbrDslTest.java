package com.hospital.mes.ebr.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class EbrDslTest {
    private EbrDsl.Context context() {
        return new EbrDsl.Context(Map.of("weight", List.of(new BigDecimal("2.345")), "repeat", List.of(1, 2, 3)),
            Set.of("QA"), Map.of("form", "DRAFT"), Instant.parse("2026-10-03T00:00:00Z"),
            (value, from, to) -> value.multiply(new BigDecimal("1000")));
    }
    @Test void tcEbr002RejectsExecutableInputAndUnknownFunctions() {
        for (String expression : List.of("eval('1')", "T(java.lang.Runtime)", "SELECT * FROM sys_user", "value('x').getClass()", "new java.io.File('x')", "fetch('http://x')", "1;2"))
            assertThatThrownBy(() -> EbrDsl.parse(expression)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void decimalCalculationAndComparisonAreDeterministic() {
        assertThat(EbrDsl.parse("round(value('weight'),2) + abs(-2)").evaluate(context())).isEqualTo(new BigDecimal("4.35"));
        assertThat(EbrDsl.parse("sum('repeat') == 6 && value('weight') > 2").evaluate(context())).isEqualTo(true);
    }
    @Test void referencesRolesStatusesAndTimeUseExplicitContext() {
        var expression=EbrDsl.parse("exists('weight') && hasRole('QA') && status('form') == 'DRAFT'");
        assertThat(expression.evaluate(context())).isEqualTo(true);
        assertThat(expression.references()).containsExactly("weight");
        assertThat(EbrDsl.parse("now()").evaluate(context())).isEqualTo("2026-10-03T00:00:00Z");
        assertThat(EbrDsl.parse("convert(2,'1','2')").evaluate(context())).isEqualTo(new BigDecimal("2000"));
    }
    @Test void missingReferencesDivisionByZeroAndInvalidArityFailClosed() {
        assertThatThrownBy(()->EbrDsl.parse("value('missing')+1").evaluate(context())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->EbrDsl.parse("1/0").evaluate(context())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->EbrDsl.parse("abs(1,2)")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->EbrDsl.parse("value(now())")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void parserLimitsNestingAndLength() {
        assertThatThrownBy(()->EbrDsl.parse("(".repeat(100)+"1"+")".repeat(100))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->EbrDsl.parse("1+".repeat(3000)+"1")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void booleanOperatorsShortCircuitUnavailableValue() {
        assertThat(EbrDsl.parse("exists('missing') && value('missing') > 0").evaluate(context())).isEqualTo(false);
    }
}
