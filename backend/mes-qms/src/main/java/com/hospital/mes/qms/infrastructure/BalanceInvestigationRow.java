package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
@TableName("mes_balance_investigation")
public class BalanceInvestigationRow extends MutableIncomingRow {
    private Long balanceResultId;
    public Long getBalanceResultId() { return balanceResultId; }
    public void setBalanceResultId(Long value) { balanceResultId = value; }
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    private String investigationText;
    public String getInvestigationText() { return investigationText; }
    public void setInvestigationText(String value) { investigationText = value; }
    private String decision;
    public String getDecision() { return decision; }
    public void setDecision(String value) { decision = value; }
    private String inputDigest;
    public String getInputDigest() { return inputDigest; }
    public void setInputDigest(String value) { inputDigest = value; }
    private Long investigatedBy;
    public Long getInvestigatedBy() { return investigatedBy; }
    public void setInvestigatedBy(Long value) { investigatedBy = value; }
    private Long approvedBy;
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long value) { approvedBy = value; }
    private java.time.LocalDateTime approvedAt;
    public java.time.LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(java.time.LocalDateTime value) { approvedAt = value; }
    private Long signatureId;
    public Long getSignatureId() { return signatureId; }
    public void setSignatureId(Long value) { signatureId = value; }
    private String signatureEvidenceJson;
    public String getSignatureEvidenceJson() { return signatureEvidenceJson; }
    public void setSignatureEvidenceJson(String value) { signatureEvidenceJson = value; }
}
