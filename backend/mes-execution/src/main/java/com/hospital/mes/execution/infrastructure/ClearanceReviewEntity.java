package com.hospital.mes.execution.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.LocalDateTime;
import java.math.BigDecimal;
@TableName(value="mes_clearance_review", excludeProperty={"updatedBy","updatedAt","versionNo"}) public class ClearanceReviewEntity extends ScopedEntity {
 private Long clearanceRecordId;
 public Long getClearanceRecordId(){return clearanceRecordId;} public void setClearanceRecordId(Long value){clearanceRecordId=value;}
 private String decision;
 public String getDecision(){return decision;} public void setDecision(String value){decision=value;}
 private String reason;
 public String getReason(){return reason;} public void setReason(String value){reason=value;}
 private Long reviewedBy;
 public Long getReviewedBy(){return reviewedBy;} public void setReviewedBy(Long value){reviewedBy=value;}
 private LocalDateTime reviewedAt;
 public LocalDateTime getReviewedAt(){return reviewedAt;} public void setReviewedAt(LocalDateTime value){reviewedAt=value;}
 private Long signatureId;
 public Long getSignatureId(){return signatureId;} public void setSignatureId(Long value){signatureId=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
