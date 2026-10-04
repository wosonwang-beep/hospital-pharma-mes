package com.hospital.mes.ebr.infrastructure;
@com.baomidou.mybatisplus.annotation.TableName("ebr_form_instance")
public class RuntimeFormEntity extends com.hospital.mes.masterdata.infrastructure.ScopedEntity {
 private Long operationExecutionId; public Long getOperationExecutionId(){return operationExecutionId;} public void setOperationExecutionId(Long v){operationExecutionId=v;}
 private Long formDefId; public Long getFormDefId(){return formDefId;} public void setFormDefId(Long v){formDefId=v;}
 private String status; public String getStatus(){return status;} public void setStatus(String v){status=v;}
 private java.time.LocalDateTime submittedAt; public java.time.LocalDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(java.time.LocalDateTime v){submittedAt=v;}
 private Integer occurrenceNo; public Integer getOccurrenceNo(){return occurrenceNo;} public void setOccurrenceNo(Integer v){occurrenceNo=v;}
 private Integer revision; public Integer getRevision(){return revision;} public void setRevision(Integer v){revision=v;}
 private Long submittedBy; public Long getSubmittedBy(){return submittedBy;} public void setSubmittedBy(Long v){submittedBy=v;}
}
