package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_inspection_report")
public class InspectionReportRow extends MutableIncomingRow {
 private String reportNo;
 public String getReportNo(){return reportNo;} public void setReportNo(String value){reportNo=value;}
 private Long inspectionRequestId;
 public Long getInspectionRequestId(){return inspectionRequestId;} public void setInspectionRequestId(Long value){inspectionRequestId=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private String recordStatus;
 public String getRecordStatus(){return recordStatus;} public void setRecordStatus(String value){recordStatus=value;}
 private String overallResult;
 public String getOverallResult(){return overallResult;} public void setOverallResult(String value){overallResult=value;}
 private String evidenceDigest;
 public String getEvidenceDigest(){return evidenceDigest;} public void setEvidenceDigest(String value){evidenceDigest=value;}
 private Long supersedesReportId;
 public Long getSupersedesReportId(){return supersedesReportId;} public void setSupersedesReportId(Long value){supersedesReportId=value;}
 private Long reviewedBy;
 public Long getReviewedBy(){return reviewedBy;} public void setReviewedBy(Long value){reviewedBy=value;}
 private java.time.LocalDateTime reviewedAt;
 public java.time.LocalDateTime getReviewedAt(){return reviewedAt;} public void setReviewedAt(java.time.LocalDateTime value){reviewedAt=value;}
 private Long approvedBy;
 public Long getApprovedBy(){return approvedBy;} public void setApprovedBy(Long value){approvedBy=value;}
 private java.time.LocalDateTime approvedAt;
 public java.time.LocalDateTime getApprovedAt(){return approvedAt;} public void setApprovedAt(java.time.LocalDateTime value){approvedAt=value;}
 private Long approvalSignatureId;
 public Long getApprovalSignatureId(){return approvalSignatureId;} public void setApprovalSignatureId(Long value){approvalSignatureId=value;}
 private String approvalSignatureEvidenceJson;
 public String getApprovalSignatureEvidenceJson(){return approvalSignatureEvidenceJson;} public void setApprovalSignatureEvidenceJson(String value){approvalSignatureEvidenceJson=value;}
}
