package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
@TableName("qms_production_plan")
public class ProductionPlanRow extends MutableIncomingRow {
    private Long mainBatchId;
    public Long getMainBatchId() { return mainBatchId; }
    public void setMainBatchId(Long value) { mainBatchId = value; }
    private Long finishedMaterialId;
    public Long getFinishedMaterialId() { return finishedMaterialId; }
    public void setFinishedMaterialId(Long value) { finishedMaterialId = value; }
    private Long qcSpecificationVersionId;
    public Long getQcSpecificationVersionId() { return qcSpecificationVersionId; }
    public void setQcSpecificationVersionId(Long value) { qcSpecificationVersionId = value; }
    private String planJson;
    public String getPlanJson() { return planJson; }
    public void setPlanJson(String value) { planJson = value; }
    private String specificationSnapshotJson;
    public String getSpecificationSnapshotJson() { return specificationSnapshotJson; }
    public void setSpecificationSnapshotJson(String value) { specificationSnapshotJson = value; }
    private String contentHash;
    public String getContentHash() { return contentHash; }
    public void setContentHash(String value) { contentHash = value; }
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
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
