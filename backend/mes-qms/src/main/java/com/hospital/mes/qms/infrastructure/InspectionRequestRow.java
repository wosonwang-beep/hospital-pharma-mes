package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_inspection_request")
public class InspectionRequestRow extends MutableIncomingRow {
 private String requestNo;
 public String getRequestNo(){return requestNo;} public void setRequestNo(String value){requestNo=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Long qcSpecificationVersionId;
 public Long getQcSpecificationVersionId(){return qcSpecificationVersionId;} public void setQcSpecificationVersionId(Long value){qcSpecificationVersionId=value;}
 private Long approvedInvestigationId;
 public Long getApprovedInvestigationId(){return approvedInvestigationId;} public void setApprovedInvestigationId(Long value){approvedInvestigationId=value;}
 private String specificationContentHash;
 public String getSpecificationContentHash(){return specificationContentHash;} public void setSpecificationContentHash(String value){specificationContentHash=value;}
 private java.math.BigDecimal requestedQuantity;
 public java.math.BigDecimal getRequestedQuantity(){return requestedQuantity;} public void setRequestedQuantity(java.math.BigDecimal value){requestedQuantity=value;}
 private Long requestedUnitId;
 public Long getRequestedUnitId(){return requestedUnitId;} public void setRequestedUnitId(Long value){requestedUnitId=value;}
 private Integer requestedPackageCount;
 public Integer getRequestedPackageCount(){return requestedPackageCount;} public void setRequestedPackageCount(Integer value){requestedPackageCount=value;}
 private java.time.LocalDate requestedDate;
 public java.time.LocalDate getRequestedDate(){return requestedDate;} public void setRequestedDate(java.time.LocalDate value){requestedDate=value;}
 private String priority;
 public String getPriority(){return priority;} public void setPriority(String value){priority=value;}
 private Long requestedBy;
 public Long getRequestedBy(){return requestedBy;} public void setRequestedBy(Long value){requestedBy=value;}
 private String requestType;
 public String getRequestType(){return requestType;} public void setRequestType(String value){requestType=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private String recordStatus;
 public String getRecordStatus(){return recordStatus;} public void setRecordStatus(String value){recordStatus=value;}
 private String reason;
 public String getReason(){return reason;} public void setReason(String value){reason=value;}
 private Long submittedBy;
 public Long getSubmittedBy(){return submittedBy;} public void setSubmittedBy(Long value){submittedBy=value;}
 private java.time.LocalDateTime submittedAt;
 public java.time.LocalDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(java.time.LocalDateTime value){submittedAt=value;}
 private Long acceptedBy;
 public Long getAcceptedBy(){return acceptedBy;} public void setAcceptedBy(Long value){acceptedBy=value;}
 private java.time.LocalDateTime acceptedAt;
 public java.time.LocalDateTime getAcceptedAt(){return acceptedAt;} public void setAcceptedAt(java.time.LocalDateTime value){acceptedAt=value;}
}
