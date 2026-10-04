package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_inspection_task")
public class InspectionTaskRow extends MutableIncomingRow {
 private String inspectionTaskNo;
 public String getInspectionTaskNo(){return inspectionTaskNo;} public void setInspectionTaskNo(String value){inspectionTaskNo=value;}
 private Long inspectionRequestId;
 public Long getInspectionRequestId(){return inspectionRequestId;} public void setInspectionRequestId(Long value){inspectionRequestId=value;}
 private Long sampleId;
 public Long getSampleId(){return sampleId;} public void setSampleId(Long value){sampleId=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private String recordStatus;
 public String getRecordStatus(){return recordStatus;} public void setRecordStatus(String value){recordStatus=value;}
 private Long assignedTo;
 public Long getAssignedTo(){return assignedTo;} public void setAssignedTo(Long value){assignedTo=value;}
 private java.time.LocalDateTime startedAt;
 public java.time.LocalDateTime getStartedAt(){return startedAt;} public void setStartedAt(java.time.LocalDateTime value){startedAt=value;}
 private java.time.LocalDateTime submittedAt;
 public java.time.LocalDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(java.time.LocalDateTime value){submittedAt=value;}
 private Long reviewedBy;
 public Long getReviewedBy(){return reviewedBy;} public void setReviewedBy(Long value){reviewedBy=value;}
 private java.time.LocalDateTime reviewedAt;
 public java.time.LocalDateTime getReviewedAt(){return reviewedAt;} public void setReviewedAt(java.time.LocalDateTime value){reviewedAt=value;}
 private Long reviewSignatureId;
 public Long getReviewSignatureId(){return reviewSignatureId;} public void setReviewSignatureId(Long value){reviewSignatureId=value;}
 private String reviewSignatureEvidenceJson;
 public String getReviewSignatureEvidenceJson(){return reviewSignatureEvidenceJson;} public void setReviewSignatureEvidenceJson(String value){reviewSignatureEvidenceJson=value;}
 private String reviewedResultIdsJson;
 public String getReviewedResultIdsJson(){return reviewedResultIdsJson;} public void setReviewedResultIdsJson(String value){reviewedResultIdsJson=value;}
}
