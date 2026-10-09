package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
@TableName("prd_prescription_item")
public class PrescriptionItemEntity extends ScopedEntity {
 private Long prescriptionId; private Integer lineNo; private Long materialId; private BigDecimal requiredQty; private Long unitId; private BigDecimal overagePct; private Boolean critical;
 public Long getPrescriptionId(){return prescriptionId;} public void setPrescriptionId(Long v){prescriptionId=v;}
 public Integer getLineNo(){return lineNo;} public void setLineNo(Integer v){lineNo=v;}
 public Long getMaterialId(){return materialId;} public void setMaterialId(Long v){materialId=v;}
 public BigDecimal getRequiredQty(){return requiredQty;} public void setRequiredQty(BigDecimal v){requiredQty=v;}
 public Long getUnitId(){return unitId;} public void setUnitId(Long v){unitId=v;}
 public BigDecimal getOveragePct(){return overagePct;} public void setOveragePct(BigDecimal v){overagePct=v;}
 public Boolean getCritical(){return critical;} public void setCritical(Boolean v){critical=v;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}