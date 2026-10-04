package com.hospital.mes.production.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("prd_execution_unit") public class ExecutionUnitEntity extends ScopedEntity {
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private Long subBatchId;
 public Long getSubBatchId(){return subBatchId;} public void setSubBatchId(Long value){subBatchId=value;}
 private String unitType;
 public String getUnitType(){return unitType;} public void setUnitType(String value){unitType=value;}
 private String executionNo;
 public String getExecutionNo(){return executionNo;} public void setExecutionNo(String value){executionNo=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
