package com.hospital.mes.execution.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.LocalDateTime;
import java.math.BigDecimal;
@TableName(value="mes_clearance_record", excludeProperty={"updatedBy","updatedAt","versionNo"}) public class ClearanceRecordEntity extends ScopedEntity {
 private Long operationExecutionId;
 public Long getOperationExecutionId(){return operationExecutionId;} public void setOperationExecutionId(Long value){operationExecutionId=value;}
 private Long equipmentUsageId;
 public Long getEquipmentUsageId(){return equipmentUsageId;} public void setEquipmentUsageId(Long value){equipmentUsageId=value;}
 private Long previousRecordId;
 public Long getPreviousRecordId(){return previousRecordId;} public void setPreviousRecordId(Long value){previousRecordId=value;}
 private String outcome;
 public String getOutcome(){return outcome;} public void setOutcome(String value){outcome=value;}
 private String reason;
 public String getReason(){return reason;} public void setReason(String value){reason=value;}
 private Long performedBy;
 public Long getPerformedBy(){return performedBy;} public void setPerformedBy(Long value){performedBy=value;}
 private LocalDateTime performedAt;
 public LocalDateTime getPerformedAt(){return performedAt;} public void setPerformedAt(LocalDateTime value){performedAt=value;}
 private Long signatureId;
 public Long getSignatureId(){return signatureId;} public void setSignatureId(Long value){signatureId=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
