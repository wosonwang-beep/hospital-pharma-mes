package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("qms_finished_inspection_report") public class FinishedInspectionReportEntity extends ScopedEntity {
 private String reportNo;
 public String getReportNo(){return reportNo;} public void setReportNo(String value){reportNo=value;}
 private Long inspectionRequestId;
 public Long getInspectionRequestId(){return inspectionRequestId;} public void setInspectionRequestId(Long value){inspectionRequestId=value;}
 private Long generationNo;
 public Long getGenerationNo(){return generationNo;} public void setGenerationNo(Long value){generationNo=value;}
 private String overallConclusion;
 public String getOverallConclusion(){return overallConclusion;} public void setOverallConclusion(String value){overallConclusion=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private String evidenceDigest;
 public String getEvidenceDigest(){return evidenceDigest;} public void setEvidenceDigest(String value){evidenceDigest=value;}
 private String summaryJson;
 public String getSummaryJson(){return summaryJson;} public void setSummaryJson(String value){summaryJson=value;}
 private Long generatedBy;
 public Long getGeneratedBy(){return generatedBy;} public void setGeneratedBy(Long value){generatedBy=value;}
 private java.time.LocalDateTime generatedAt;
 public java.time.LocalDateTime getGeneratedAt(){return generatedAt;} public void setGeneratedAt(java.time.LocalDateTime value){generatedAt=value;}
 private Long approvedBy;
 public Long getApprovedBy(){return approvedBy;} public void setApprovedBy(Long value){approvedBy=value;}
 private java.time.LocalDateTime approvedAt;
 public java.time.LocalDateTime getApprovedAt(){return approvedAt;} public void setApprovedAt(java.time.LocalDateTime value){approvedAt=value;}
 private Long signatureId;
 public Long getSignatureId(){return signatureId;} public void setSignatureId(Long value){signatureId=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
 private String reason;
 public String getReason(){return reason;} public void setReason(String value){reason=value;}
}
