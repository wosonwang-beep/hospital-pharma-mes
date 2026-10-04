package com.hospital.mes.execution.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("mes_equipment_usage") public class EquipmentUsageEntity extends ScopedEntity {
 private Long executionUnitId;
 public Long getExecutionUnitId(){return executionUnitId;} public void setExecutionUnitId(Long value){executionUnitId=value;}
 private Long operationExecutionId;
 public Long getOperationExecutionId(){return operationExecutionId;} public void setOperationExecutionId(Long value){operationExecutionId=value;}
 private Long equipmentId;
 public Long getEquipmentId(){return equipmentId;} public void setEquipmentId(Long value){equipmentId=value;}
 private String usageRole;
 public String getUsageRole(){return usageRole;} public void setUsageRole(String value){usageRole=value;}
 private String clearanceStatus;
 public String getClearanceStatus(){return clearanceStatus;} public void setClearanceStatus(String value){clearanceStatus=value;}
 private String qualificationStatus;
 public String getQualificationStatus(){return qualificationStatus;} public void setQualificationStatus(String value){qualificationStatus=value;}
 private LocalDateTime boundAt;
 public LocalDateTime getBoundAt(){return boundAt;} public void setBoundAt(LocalDateTime value){boundAt=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
