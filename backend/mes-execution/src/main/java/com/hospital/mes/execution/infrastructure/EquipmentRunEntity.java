package com.hospital.mes.execution.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("mes_equipment_run") public class EquipmentRunEntity extends ScopedEntity {
 private Long equipmentUsageId;
 public Long getEquipmentUsageId(){return equipmentUsageId;} public void setEquipmentUsageId(Long value){equipmentUsageId=value;}
 private String runNo;
 public String getRunNo(){return runNo;} public void setRunNo(String value){runNo=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private LocalDateTime startedAt;
 public LocalDateTime getStartedAt(){return startedAt;} public void setStartedAt(LocalDateTime value){startedAt=value;}
 private LocalDateTime endedAt;
 public LocalDateTime getEndedAt(){return endedAt;} public void setEndedAt(LocalDateTime value){endedAt=value;}
 private String sourceMessageId;
 public String getSourceMessageId(){return sourceMessageId;} public void setSourceMessageId(String value){sourceMessageId=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
