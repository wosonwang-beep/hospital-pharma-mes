package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.LocalDateTime;
import java.math.BigDecimal;
@TableName(value="qms_ipc_review", excludeProperty={"updatedBy","updatedAt","versionNo"}) public class IpcReviewEntity extends ScopedEntity {
 private Long ipcResultRevisionId;
 public Long getIpcResultRevisionId(){return ipcResultRevisionId;} public void setIpcResultRevisionId(Long value){ipcResultRevisionId=value;}
 private String disposition;
 public String getDisposition(){return disposition;} public void setDisposition(String value){disposition=value;}
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
