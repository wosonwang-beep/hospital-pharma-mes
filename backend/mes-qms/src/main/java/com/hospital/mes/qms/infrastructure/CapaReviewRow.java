package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
@TableName("qms_capa_review")
public class CapaReviewRow extends IncomingRow {
    private Long capaId;
    public Long getCapaId() { return capaId; }
    public void setCapaId(Long value) { capaId = value; }
    private Integer reviewNo;
    public Integer getReviewNo() { return reviewNo; }
    public void setReviewNo(Integer value) { reviewNo = value; }
    private String decision;
    public String getDecision() { return decision; }
    public void setDecision(String value) { decision = value; }
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
