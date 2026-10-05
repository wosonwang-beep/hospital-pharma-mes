package com.hospital.mes.qms.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.common.exception.ComplianceException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

/** Closed, versioned quantity expression language. Contains no executable expressions. */
public final class BalanceFormula {
    private static final MathContext MC = MathContext.DECIMAL128;
    private static final Set<String> TYPES = Set.of("CHARGE", "ISSUE", "RETURN", "OUTPUT", "SAMPLE", "LOSS", "SCRAP", "WIP");
    private BalanceFormula() {}

    public record Calculation(BigDecimal expected, BigDecimal actual, BigDecimal difference,
                              BigDecimal differencePct, String status) {}
    private record Value(BigDecimal amount, boolean quantity) {}
    private record Expression(String op, BigDecimal constant, List<String> sources,
                              Expression left, Expression right, boolean quantity) {
        Value evaluate(Map<String, BigDecimal> inputs) {
            if (constant != null) return new Value(constant, false);
            if (sources != null) {
                BigDecimal sum = BigDecimal.ZERO;
                for (String type : sources) {
                    BigDecimal input = inputs.get(type);
                    if (input == null) throw new ComplianceException("BALANCE_INPUT_MISSING", "Missing measured input: " + type);
                    sum = sum.add(input);
                }
                return new Value(sum, true);
            }
            BigDecimal a = left.evaluate(inputs).amount(), b = right.evaluate(inputs).amount();
            BigDecimal result = switch (op) {
                case "ADD" -> a.add(b, MC);
                case "SUBTRACT" -> a.subtract(b, MC);
                case "MULTIPLY" -> a.multiply(b, MC);
                case "DIVIDE" -> {
                    if (b.signum() == 0) throw new ComplianceException("BALANCE_INPUT_INVALID", "Division by zero");
                    yield a.divide(b, MC);
                }
                default -> throw new IllegalStateException("Unvalidated operator");
            };
            return new Value(result, quantity);
        }
    }
    private record Formula(Expression expected, Expression actual, String metric) {}
    private static final class Budget { int nodes; }

    public static void validate(JsonNode json) { compile(json); }
    public static Set<String> sources(JsonNode json) {
        Formula formula = compile(json);
        var result = new TreeSet<String>();
        collect(formula.expected(), result); collect(formula.actual(), result);
        return Collections.unmodifiableSet(result);
    }
    public static List<Set<String>> sourceGroups(JsonNode json) {
        Formula formula = compile(json);
        var groups = new ArrayList<Set<String>>();
        groups(formula.expected(), groups); groups(formula.actual(), groups);
        return List.copyOf(groups);
    }
    private static void groups(Expression expression, List<Set<String>> groups) {
        if (expression.sources()!=null) groups.add(Set.copyOf(expression.sources()));
        if (expression.left()!=null) { groups(expression.left(), groups); groups(expression.right(), groups); }
    }
    private static void collect(Expression expression, Set<String> result) {
        if (expression.sources() != null) result.addAll(expression.sources());
        if (expression.left() != null) { collect(expression.left(), result); collect(expression.right(), result); }
    }
    public static Calculation calculate(JsonNode json, Map<String, BigDecimal> inputs,
                                        BigDecimal low, BigDecimal high) {
        if (low == null || high == null || low.compareTo(high)>0) throw new IllegalArgumentException("Invalid frozen tolerances");
        Formula formula = compile(json);
        BigDecimal expected = formula.expected().evaluate(inputs).amount();
        BigDecimal actual = formula.actual().evaluate(inputs).amount();
        if (expected.signum()<=0) throw new ComplianceException("BALANCE_INPUT_INVALID", "Expected quantity must be positive");
        BigDecimal difference = actual.subtract(expected, MC);
        BigDecimal pct = difference.divide(expected, MC).multiply(new BigDecimal("100"), MC);
        BigDecimal metric = formula.metric().equals("YIELD_PCT")
                ? actual.divide(expected, MC).multiply(new BigDecimal("100"), MC) : pct;
        String status = metric.compareTo(low)>=0 && metric.compareTo(high)<=0 ? "PASS" : "FAIL";
        BigDecimal storedExpected = stored(expected);
        if (storedExpected.signum()<=0) throw new ComplianceException("BALANCE_INPUT_INVALID", "Stored expected quantity must remain positive");
        return new Calculation(storedExpected, stored(actual), stored(difference), stored(pct), status);
    }
    private static BigDecimal stored(BigDecimal value) {
        BigDecimal result = value.setScale(8, RoundingMode.HALF_EVEN);
        if (result.precision()>24) throw new ComplianceException("BALANCE_INPUT_INVALID", "Calculation exceeds DECIMAL(24,8)");
        return result;
    }
    private static Formula compile(JsonNode json) {
        fields(json, Set.of("dslVersion", "expected", "actual", "metric"));
        if (!json.path("dslVersion").isIntegralNumber() || json.path("dslVersion").asInt()!=1)
            throw new IllegalArgumentException("Unsupported balance DSL version");
        String metric = json.path("metric").asText();
        if (!Set.of("DIFFERENCE_PCT", "YIELD_PCT").contains(metric)) throw new IllegalArgumentException("Unknown balance metric");
        Budget budget = new Budget();
        Expression expected = expression(json.get("expected"), 1, budget);
        Expression actual = expression(json.get("actual"), 1, budget);
        if (!expected.quantity() || !actual.quantity()) throw new IllegalArgumentException("Final expression dimension must be quantity");
        return new Formula(expected, actual, metric);
    }
    private static Expression expression(JsonNode json, int depth, Budget budget) {
        if (++budget.nodes>64 || depth>8) throw new IllegalArgumentException("Balance expression complexity limit exceeded");
        if (json==null || !json.isObject()) throw new IllegalArgumentException("Expression must be a closed JSON object");
        if (json.has("sum")) {
            fields(json, Set.of("sum"));
            JsonNode sources = json.get("sum");
            if (!sources.isArray() || sources.isEmpty()) throw new IllegalArgumentException("sum needs measured event types");
            List<String> types = new ArrayList<>();
            for (JsonNode type : sources) {
                if (!type.isTextual() || !TYPES.contains(type.asText()) || types.contains(type.asText()))
                    throw new IllegalArgumentException("Unknown or duplicate event source");
                types.add(type.asText());
            }
            return new Expression(null, null, List.copyOf(types), null, null, true);
        }
        if (json.has("constant")) {
            fields(json, Set.of("constant"));
            JsonNode raw = json.get("constant");
            if (!raw.isTextual() || !raw.asText().matches("-?\\d+(\\.\\d{1,8})?")) throw new IllegalArgumentException("Exact decimal string required");
            BigDecimal value = new BigDecimal(raw.asText());
            if (raw.asText().replace("-", "").replace(".", "").length()>24 || value.precision()>24) throw new IllegalArgumentException("Constant decimal precision exceeded");
            return new Expression(null, value, null, null, null, false);
        }
        fields(json, Set.of("op", "left", "right"));
        String op = json.path("op").asText();
        Expression left = expression(json.get("left"), depth+1, budget), right = expression(json.get("right"), depth+1, budget);
        boolean quantity;
        switch (op) {
            case "ADD", "SUBTRACT" -> {
                if (left.quantity()!=right.quantity()) throw new IllegalArgumentException("Addition/subtraction dimension mismatch");
                quantity = left.quantity();
            }
            case "MULTIPLY" -> {
                if (left.quantity() && right.quantity()) throw new IllegalArgumentException("Multiplication dimension mismatch");
                quantity = left.quantity() || right.quantity();
            }
            case "DIVIDE" -> {
                if (!left.quantity() && right.quantity()) throw new IllegalArgumentException("Division dimension mismatch");
                quantity = left.quantity() && !right.quantity();
            }
            default -> throw new IllegalArgumentException("Unknown balance operator");
        }
        return new Expression(op, null, null, left, right, quantity);
    }
    private static void fields(JsonNode node, Set<String> fields) {
        if (node==null || !node.isObject() || node.size()!=fields.size()) throw new IllegalArgumentException("Closed expression fields required");
        var names = node.fieldNames();
        while (names.hasNext()) if (!fields.contains(names.next())) throw new IllegalArgumentException("Unknown expression property");
    }
}
