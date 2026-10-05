package com.hospital.mes.qms.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class BalanceFormulaTest {
    final ObjectMapper json = new ObjectMapper();
    JsonNode formula(String expected, String actual, String metric) throws Exception {
        return json.readTree("{\"dslVersion\":1,\"expected\":" + expected + ",\"actual\":" + actual + ",\"metric\":\"" + metric + "\"}");
    }
    JsonNode ordinary() throws Exception { return formula("{\"sum\":[\"CHARGE\"]}", "{\"sum\":[\"OUTPUT\",\"LOSS\"]}", "DIFFERENCE_PCT"); }
    @Test void exactBoundaryIsInclusiveAndReversalsUseNetSourceAmounts() throws Exception {
        var result = BalanceFormula.calculate(ordinary(), Map.of("CHARGE", new BigDecimal("100"), "OUTPUT", new BigDecimal("99"), "LOSS", BigDecimal.ZERO), new BigDecimal("-1"), BigDecimal.ONE);
        assertThat(result.status()).isEqualTo("PASS");
        assertThat(result.differencePct()).isEqualByComparingTo("-1");
        assertThat(result.expected()).isEqualByComparingTo("100");
    }
    @Test void roundingMustNeverTurnAnExceededLimitIntoPass() throws Exception {
        var result = BalanceFormula.calculate(formula("{\"sum\":[\"CHARGE\"]}", "{\"sum\":[\"OUTPUT\"]}", "DIFFERENCE_PCT"), Map.of("CHARGE", new BigDecimal("100000000000"), "OUTPUT", new BigDecimal("101000000000.000001")), new BigDecimal("-1"), BigDecimal.ONE);
        assertThat(result.status()).isEqualTo("FAIL");
        assertThat(result.differencePct()).isEqualByComparingTo("1.00000000");
    }
    @Test void missingInputIsNotAssumedZero() throws Exception {
        var f = ordinary();
        assertThatThrownBy(() -> BalanceFormula.calculate(f, Map.of("CHARGE", BigDecimal.TEN, "OUTPUT", BigDecimal.TEN), BigDecimal.ZERO, BigDecimal.ZERO)).hasMessageContaining("LOSS");
    }
    @Test void yieldUsesFrozenMetricAndLimits() throws Exception {
        var f = formula("{\"sum\":[\"CHARGE\"]}", "{\"sum\":[\"OUTPUT\"]}", "YIELD_PCT");
        assertThat(BalanceFormula.calculate(f, Map.of("CHARGE", new BigDecimal("100"), "OUTPUT", new BigDecimal("95")), new BigDecimal("94"), new BigDecimal("96")).status()).isEqualTo("PASS");
    }
    @Test void dimensionalMismatchAndQuantityMultiplicationAreRejected() throws Exception {
        for (String op : new String[]{"ADD", "MULTIPLY"}) {
            var f = formula("{\"sum\":[\"CHARGE\"]}", "{\"op\":\""+op+"\",\"left\":{\"sum\":[\"OUTPUT\"]},\"right\":"+(op.equals("ADD")?"{\"constant\":\"1\"}":"{\"sum\":[\"OUTPUT\"]}")+"}", "YIELD_PCT");
            assertThatThrownBy(() -> BalanceFormula.calculate(f, Map.of("CHARGE", BigDecimal.TEN, "OUTPUT", BigDecimal.TEN), BigDecimal.ZERO, BigDecimal.ONE)).hasMessageContaining("dimension");
        }
    }
    @Test void scalarMultiplicationAndDimensionlessDivisionWork() throws Exception {
        var f = formula("{\"sum\":[\"CHARGE\"]}", "{\"op\":\"MULTIPLY\",\"left\":{\"sum\":[\"OUTPUT\"]},\"right\":{\"op\":\"DIVIDE\",\"left\":{\"sum\":[\"CHARGE\"]},\"right\":{\"sum\":[\"OUTPUT\"]}}}", "DIFFERENCE_PCT");
        assertThat(BalanceFormula.calculate(f, Map.of("CHARGE", BigDecimal.TEN, "OUTPUT", new BigDecimal("5")), BigDecimal.ZERO, BigDecimal.ZERO).status()).isEqualTo("PASS");
    }
    @Test void rejectsExecutableStringsUnknownFieldsAndDuplicateSources() throws Exception {
        for (String expression : new String[]{"\"CHARGE + OUTPUT\"", "{\"sum\":[\"OUTPUT\"],\"sql\":\"select 1\"}", "{\"sum\":[\"OUTPUT\",\"OUTPUT\"]}"}) {
            var f = formula("{\"sum\":[\"CHARGE\"]}", expression, "DIFFERENCE_PCT");
            assertThatThrownBy(() -> BalanceFormula.calculate(f, Map.of("CHARGE", BigDecimal.TEN, "OUTPUT", BigDecimal.TEN), BigDecimal.ZERO, BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
        }
    }
    @Test void zeroDivisorAndNonpositiveExpectedFailClosed() throws Exception {
        var f = formula("{\"sum\":[\"CHARGE\"]}", "{\"op\":\"DIVIDE\",\"left\":{\"sum\":[\"OUTPUT\"]},\"right\":{\"constant\":\"0\"}}", "DIFFERENCE_PCT");
        assertThatThrownBy(() -> BalanceFormula.calculate(f, Map.of("CHARGE", BigDecimal.TEN, "OUTPUT", BigDecimal.ONE), BigDecimal.ZERO, BigDecimal.ZERO)).hasMessageContaining("zero");
        var plain = ordinary();
        assertThatThrownBy(() -> BalanceFormula.calculate(plain, Map.of("CHARGE", BigDecimal.ZERO, "OUTPUT", BigDecimal.ONE, "LOSS", BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO)).hasMessageContaining("positive");
    }
    @Test void boundsDepthNodesAndDecimalPrecision() throws Exception {
        String expr = "{\"sum\":[\"OUTPUT\"]}";
        for (int i=0; i<9; i++) expr = "{\"op\":\"MULTIPLY\",\"left\":"+expr+",\"right\":{\"constant\":\"1\"}}";
        var f = formula("{\"sum\":[\"CHARGE\"]}", expr, "DIFFERENCE_PCT");
        assertThatThrownBy(() -> BalanceFormula.validate(f)).hasMessageContaining("limit");
        for (String constant : new String[]{"1e3", "0.000000001", "1234567890123456789012345", "00000000000000000000000001"}) {
            var invalid = formula("{\"sum\":[\"CHARGE\"]}", "{\"op\":\"MULTIPLY\",\"left\":{\"sum\":[\"OUTPUT\"]},\"right\":{\"constant\":\""+constant+"\"}}", "DIFFERENCE_PCT");
            assertThatThrownBy(() -> BalanceFormula.validate(invalid)).isInstanceOf(IllegalArgumentException.class);
        }
    }
    @Test void expectedThatRoundsToZeroIsRejectedBeforePersistence() throws Exception {
        var f = formula("{\"op\":\"MULTIPLY\",\"left\":{\"sum\":[\"CHARGE\"]},\"right\":{\"constant\":\"0.00000001\"}}", "{\"sum\":[\"OUTPUT\"]}", "DIFFERENCE_PCT");
        assertThatThrownBy(() -> BalanceFormula.calculate(f, Map.of("CHARGE", new BigDecimal("0.000001"), "OUTPUT", new BigDecimal("0.000001")), BigDecimal.ZERO, BigDecimal.ZERO)).hasMessageContaining("positive");
    }
}
