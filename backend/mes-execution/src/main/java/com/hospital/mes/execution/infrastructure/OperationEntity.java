package com.hospital.mes.execution.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("mes_operation_execution") public class OperationEntity extends ScopedEntity {
 private Long executionUnitId;
 public Long getExecutionUnitId(){return executionUnitId;} public void setExecutionUnitId(Long value){executionUnitId=value;}
 private Long operationDefId;
 public Long getOperationDefId(){return operationDefId;} public void setOperationDefId(Long value){operationDefId=value;}
 private Integer operationSeq;
 public Integer getOperationSeq(){return operationSeq;} public void setOperationSeq(Integer value){operationSeq=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private LocalDateTime startedAt;
 public LocalDateTime getStartedAt(){return startedAt;} public void setStartedAt(LocalDateTime value){startedAt=value;}
 private LocalDateTime completedAt;
 public LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(LocalDateTime value){completedAt=value;}
 private Long operatorId;
 public Long getOperatorId(){return operatorId;} public void setOperatorId(Long value){operatorId=value;}
 private String gateEvidenceJson;
 public String getGateEvidenceJson(){return gateEvidenceJson;} public void setGateEvidenceJson(String value){gateEvidenceJson=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
