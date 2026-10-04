package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("proc_parameter_def")
public class ParameterEntity extends ScopedEntity {
 private Long operationDefId;
 public Long getOperationDefId(){return operationDefId;} public void setOperationDefId(Long v){operationDefId=v;}
 private String parameterCode;
 public String getParameterCode(){return parameterCode;} public void setParameterCode(String v){parameterCode=v;}
 private String parameterName;
 public String getParameterName(){return parameterName;} public void setParameterName(String v){parameterName=v;}
 private String acquisitionMode;
 public String getAcquisitionMode(){return acquisitionMode;} public void setAcquisitionMode(String v){acquisitionMode=v;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long v){unitId=v;}
 private java.math.BigDecimal lowerLimit;
 public java.math.BigDecimal getLowerLimit(){return lowerLimit;} public void setLowerLimit(java.math.BigDecimal v){lowerLimit=v;}
 private java.math.BigDecimal upperLimit;
 public java.math.BigDecimal getUpperLimit(){return upperLimit;} public void setUpperLimit(java.math.BigDecimal v){upperLimit=v;}
}
