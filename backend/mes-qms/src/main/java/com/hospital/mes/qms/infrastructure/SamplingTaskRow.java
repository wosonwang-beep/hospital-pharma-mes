package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_sampling_task")
public class SamplingTaskRow extends MutableIncomingRow {
 private String samplingTaskNo;
 public String getSamplingTaskNo(){return samplingTaskNo;} public void setSamplingTaskNo(String value){samplingTaskNo=value;}
 private Long inspectionRequestId;
 public Long getInspectionRequestId(){return inspectionRequestId;} public void setInspectionRequestId(Long value){inspectionRequestId=value;}
 private Long inspectionRequestItemId;
 public Long getInspectionRequestItemId(){return inspectionRequestItemId;} public void setInspectionRequestItemId(Long value){inspectionRequestItemId=value;}
 private String samplingPlan;
 public String getSamplingPlan(){return samplingPlan;} public void setSamplingPlan(String value){samplingPlan=value;}
 private Integer requiredPackageCount;
 public Integer getRequiredPackageCount(){return requiredPackageCount;} public void setRequiredPackageCount(Integer value){requiredPackageCount=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private String recordStatus;
 public String getRecordStatus(){return recordStatus;} public void setRecordStatus(String value){recordStatus=value;}
 private Long assignedTo;
 public Long getAssignedTo(){return assignedTo;} public void setAssignedTo(Long value){assignedTo=value;}
 private java.time.LocalDateTime startedAt;
 public java.time.LocalDateTime getStartedAt(){return startedAt;} public void setStartedAt(java.time.LocalDateTime value){startedAt=value;}
 private java.time.LocalDateTime completedAt;
 public java.time.LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(java.time.LocalDateTime value){completedAt=value;}
 private Long planSignatureId;
 public Long getPlanSignatureId(){return planSignatureId;} public void setPlanSignatureId(Long value){planSignatureId=value;}
 private String planSignatureEvidenceJson;
 public String getPlanSignatureEvidenceJson(){return planSignatureEvidenceJson;} public void setPlanSignatureEvidenceJson(String value){planSignatureEvidenceJson=value;}
 private Long completionSignatureId;
 public Long getCompletionSignatureId(){return completionSignatureId;} public void setCompletionSignatureId(Long value){completionSignatureId=value;}
 private String completionSignatureEvidenceJson;
 public String getCompletionSignatureEvidenceJson(){return completionSignatureEvidenceJson;} public void setCompletionSignatureEvidenceJson(String value){completionSignatureEvidenceJson=value;}
 private String otherSampleName;
 public String getOtherSampleName(){return otherSampleName;} public void setOtherSampleName(String value){otherSampleName=value;}
 private String otherSampleReason;
 public String getOtherSampleReason(){return otherSampleReason;} public void setOtherSampleReason(String value){otherSampleReason=value;}
}
