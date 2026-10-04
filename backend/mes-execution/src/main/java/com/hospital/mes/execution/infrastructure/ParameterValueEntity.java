package com.hospital.mes.execution.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("mes_parameter_value") public class ParameterValueEntity extends ScopedEntity {
 private Long operationExecutionId;
 public Long getOperationExecutionId(){return operationExecutionId;} public void setOperationExecutionId(Long value){operationExecutionId=value;}
 private Long parameterDefId;
 public Long getParameterDefId(){return parameterDefId;} public void setParameterDefId(Long value){parameterDefId=value;}
 private String sourceMode;
 public String getSourceMode(){return sourceMode;} public void setSourceMode(String value){sourceMode=value;}
 private BigDecimal rawValue;
 public BigDecimal getRawValue(){return rawValue;} public void setRawValue(BigDecimal value){rawValue=value;}
 private String textValue;
 public String getTextValue(){return textValue;} public void setTextValue(String value){textValue=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private Long equipmentId;
 public Long getEquipmentId(){return equipmentId;} public void setEquipmentId(Long value){equipmentId=value;}
 private String sourceMessageId;
 public String getSourceMessageId(){return sourceMessageId;} public void setSourceMessageId(String value){sourceMessageId=value;}
 private LocalDateTime capturedAt;
 public LocalDateTime getCapturedAt(){return capturedAt;} public void setCapturedAt(LocalDateTime value){capturedAt=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
