package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("qms_finished_report_review") public class FinishedReportReviewEntity extends ScopedEntity {
 private Long reportId;
 public Long getReportId(){return reportId;} public void setReportId(Long value){reportId=value;}
 private String reviewDecision;
 public String getReviewDecision(){return reviewDecision;} public void setReviewDecision(String value){reviewDecision=value;}
 private Long reviewedBy;
 public Long getReviewedBy(){return reviewedBy;} public void setReviewedBy(Long value){reviewedBy=value;}
 private java.time.LocalDateTime reviewedAt;
 public java.time.LocalDateTime getReviewedAt(){return reviewedAt;} public void setReviewedAt(java.time.LocalDateTime value){reviewedAt=value;}
 private Long signatureId;
 public Long getSignatureId(){return signatureId;} public void setSignatureId(Long value){signatureId=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
 private String reason;
 public String getReason(){return reason;} public void setReason(String value){reason=value;}
}
