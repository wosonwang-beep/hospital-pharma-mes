package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
@TableName("mes_balance_rule")
public class BalanceRuleRow extends IncomingRow {
    private Long processSnapshotId;
    public Long getProcessSnapshotId() { return processSnapshotId; }
    public void setProcessSnapshotId(Long value) { processSnapshotId = value; }
    private Long mainBatchId;
    public Long getMainBatchId() { return mainBatchId; }
    public void setMainBatchId(Long value) { mainBatchId = value; }
    private String balanceCode;
    public String getBalanceCode() { return balanceCode; }
    public void setBalanceCode(String value) { balanceCode = value; }
    private String basis;
    public String getBasis() { return basis; }
    public void setBasis(String value) { basis = value; }
    private Long operationExecutionId;
    public Long getOperationExecutionId() { return operationExecutionId; }
    public void setOperationExecutionId(Long value) { operationExecutionId = value; }
    private Long materialId;
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long value) { materialId = value; }
    private Long unitId;
    public Long getUnitId() { return unitId; }
    public void setUnitId(Long value) { unitId = value; }
    private String formulaExpr;
    public String getFormulaExpr() { return formulaExpr; }
    public void setFormulaExpr(String value) { formulaExpr = value; }
    private java.math.BigDecimal toleranceLow;
    public java.math.BigDecimal getToleranceLow() { return toleranceLow; }
    public void setToleranceLow(java.math.BigDecimal value) { toleranceLow = value; }
    private java.math.BigDecimal toleranceHigh;
    public java.math.BigDecimal getToleranceHigh() { return toleranceHigh; }
    public void setToleranceHigh(java.math.BigDecimal value) { toleranceHigh = value; }
    private Integer ruleVersion;
    public Integer getRuleVersion() { return ruleVersion; }
    public void setRuleVersion(Integer value) { ruleVersion = value; }
    private String checkPoint;
    public String getCheckPoint() { return checkPoint; }
    public void setCheckPoint(String value) { checkPoint = value; }
}
