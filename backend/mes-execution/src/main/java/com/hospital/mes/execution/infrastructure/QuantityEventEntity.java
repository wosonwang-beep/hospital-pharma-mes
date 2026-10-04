package com.hospital.mes.execution.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("mes_quantity_event") public class QuantityEventEntity extends ScopedEntity {
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private Long executionUnitId;
 public Long getExecutionUnitId(){return executionUnitId;} public void setExecutionUnitId(Long value){executionUnitId=value;}
 private Long operationExecutionId;
 public Long getOperationExecutionId(){return operationExecutionId;} public void setOperationExecutionId(Long value){operationExecutionId=value;}
 private String eventType;
 public String getEventType(){return eventType;} public void setEventType(String value){eventType=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private BigDecimal amount;
 public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal value){amount=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private String sourceType;
 public String getSourceType(){return sourceType;} public void setSourceType(String value){sourceType=value;}
 private String sourceRef;
 public String getSourceRef(){return sourceRef;} public void setSourceRef(String value){sourceRef=value;}
 private LocalDateTime occurredAt;
 public LocalDateTime getOccurredAt(){return occurredAt;} public void setOccurredAt(LocalDateTime value){occurredAt=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
