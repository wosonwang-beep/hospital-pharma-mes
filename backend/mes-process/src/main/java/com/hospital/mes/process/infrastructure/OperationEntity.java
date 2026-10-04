package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("proc_operation_def")
public class OperationEntity extends ScopedEntity {
 private Long routeVersionId;
 public Long getRouteVersionId(){return routeVersionId;} public void setRouteVersionId(Long v){routeVersionId=v;}
 private String operationCode;
 public String getOperationCode(){return operationCode;} public void setOperationCode(String v){operationCode=v;}
 private String operationName;
 public String getOperationName(){return operationName;} public void setOperationName(String v){operationName=v;}
 private Integer sequenceNo;
 public Integer getSequenceNo(){return sequenceNo;} public void setSequenceNo(Integer v){sequenceNo=v;}
 private String requiredRole;
 public String getRequiredRole(){return requiredRole;} public void setRequiredRole(String v){requiredRole=v;}
 private String completionRule;
 public String getCompletionRule(){return completionRule;} public void setCompletionRule(String v){completionRule=v;}
 private String predecessorCodesJson;
 public String getPredecessorCodesJson(){return predecessorCodesJson;} public void setPredecessorCodesJson(String v){predecessorCodesJson=v;}
 private String requiredEquipmentType;
 public String getRequiredEquipmentType(){return requiredEquipmentType;} public void setRequiredEquipmentType(String v){requiredEquipmentType=v;}
 private String ipcDefinitionsJson;
 public String getIpcDefinitionsJson(){return ipcDefinitionsJson;} public void setIpcDefinitionsJson(String v){ipcDefinitionsJson=v;}
 private Boolean clearanceRequired;
 public Boolean getClearanceRequired(){return clearanceRequired;} public void setClearanceRequired(Boolean v){clearanceRequired=v;}
}
