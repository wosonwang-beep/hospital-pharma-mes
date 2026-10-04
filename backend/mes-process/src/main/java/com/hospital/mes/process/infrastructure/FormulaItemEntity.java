package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("proc_formula_item")
public class FormulaItemEntity extends ScopedEntity {
 private Long formulaVersionId;
 public Long getFormulaVersionId(){return formulaVersionId;} public void setFormulaVersionId(Long v){formulaVersionId=v;}
 private Integer lineNo;
 public Integer getLineNo(){return lineNo;} public void setLineNo(Integer v){lineNo=v;}
 private Long materialId;
 public Long getMaterialId(){return materialId;} public void setMaterialId(Long v){materialId=v;}
 private java.math.BigDecimal requiredQty;
 public java.math.BigDecimal getRequiredQty(){return requiredQty;} public void setRequiredQty(java.math.BigDecimal v){requiredQty=v;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long v){unitId=v;}
 private java.math.BigDecimal overagePct;
 public java.math.BigDecimal getOveragePct(){return overagePct;} public void setOveragePct(java.math.BigDecimal v){overagePct=v;}
 private Boolean critical;
 public Boolean getCritical(){return critical;} public void setCritical(Boolean v){critical=v;}
}
