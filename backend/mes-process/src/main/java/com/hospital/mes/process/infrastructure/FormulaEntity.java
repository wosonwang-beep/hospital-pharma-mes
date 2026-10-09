package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("proc_formula_version")
public class FormulaEntity extends ScopedEntity {
 private Long currentDefinitionId;
 public Long getCurrentDefinitionId(){return currentDefinitionId;} public void setCurrentDefinitionId(Long v){currentDefinitionId=v;}
 private Long packageVersionId;
 public Long getPackageVersionId(){return packageVersionId;} public void setPackageVersionId(Long v){packageVersionId=v;}
 private String formulaCode;
 public String getFormulaCode(){return formulaCode;} public void setFormulaCode(String v){formulaCode=v;}
 @TableField("version")
 private Integer businessVersion;
 public Integer getBusinessVersion(){return businessVersion;} public void setBusinessVersion(Integer v){businessVersion=v;}
 private java.math.BigDecimal batchBasisQty;
 public java.math.BigDecimal getBatchBasisQty(){return batchBasisQty;} public void setBatchBasisQty(java.math.BigDecimal v){batchBasisQty=v;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long v){unitId=v;}
}
