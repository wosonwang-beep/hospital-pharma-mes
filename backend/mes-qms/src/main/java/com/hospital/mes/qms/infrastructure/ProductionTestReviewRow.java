package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
@TableName("qms_production_test_review")
public class ProductionTestReviewRow extends IncomingRow {
    private Long resultRevisionId;
    public Long getResultRevisionId() { return resultRevisionId; }
    public void setResultRevisionId(Long value) { resultRevisionId = value; }
    private String disposition;
    public String getDisposition() { return disposition; }
    public void setDisposition(String value) { disposition = value; }
    private String reason;
    public String getReason() { return reason; }
    public void setReason(String value) { reason = value; }
    private Long reviewedBy;
    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long value) { reviewedBy = value; }
    private java.time.LocalDateTime reviewedAt;
    public java.time.LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(java.time.LocalDateTime value) { reviewedAt = value; }
    private Long signatureId;
    public Long getSignatureId() { return signatureId; }
    public void setSignatureId(Long value) { signatureId = value; }
    private String signatureEvidenceJson;
    public String getSignatureEvidenceJson() { return signatureEvidenceJson; }
    public void setSignatureEvidenceJson(String value) { signatureEvidenceJson = value; }
}
