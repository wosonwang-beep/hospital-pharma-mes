package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
@TableName("mes_balance_result")
public class BalanceResultRow extends IncomingRow {
    private Long mainBatchId;
    public Long getMainBatchId() { return mainBatchId; }
    public void setMainBatchId(Long value) { mainBatchId = value; }
    private Long balanceRuleId;
    public Long getBalanceRuleId() { return balanceRuleId; }
    public void setBalanceRuleId(Long value) { balanceRuleId = value; }
    private Integer calculationVersion;
    public Integer getCalculationVersion() { return calculationVersion; }
    public void setCalculationVersion(Integer value) { calculationVersion = value; }
    private java.math.BigDecimal expectedValue;
    public java.math.BigDecimal getExpectedValue() { return expectedValue; }
    public void setExpectedValue(java.math.BigDecimal value) { expectedValue = value; }
    private java.math.BigDecimal actualValue;
    public java.math.BigDecimal getActualValue() { return actualValue; }
    public void setActualValue(java.math.BigDecimal value) { actualValue = value; }
    private java.math.BigDecimal differenceValue;
    public java.math.BigDecimal getDifferenceValue() { return differenceValue; }
    public void setDifferenceValue(java.math.BigDecimal value) { differenceValue = value; }
    private java.math.BigDecimal differencePct;
    public java.math.BigDecimal getDifferencePct() { return differencePct; }
    public void setDifferencePct(java.math.BigDecimal value) { differencePct = value; }
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    private java.time.LocalDateTime calculatedAt;
    public java.time.LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(java.time.LocalDateTime value) { calculatedAt = value; }
    private Long calculatedBy;
    public Long getCalculatedBy() { return calculatedBy; }
    public void setCalculatedBy(Long value) { calculatedBy = value; }
    private String inputSnapshotJson;
    public String getInputSnapshotJson() { return inputSnapshotJson; }
    public void setInputSnapshotJson(String value) { inputSnapshotJson = value; }
    private String inputDigest;
    public String getInputDigest() { return inputDigest; }
    public void setInputDigest(String value) { inputDigest = value; }
    private String ruleSnapshotJson;
    public String getRuleSnapshotJson() { return ruleSnapshotJson; }
    public void setRuleSnapshotJson(String value) { ruleSnapshotJson = value; }
}
