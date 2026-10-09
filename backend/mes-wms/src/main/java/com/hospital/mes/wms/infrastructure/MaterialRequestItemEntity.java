package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("wms_material_request_item") public class MaterialRequestItemEntity extends ScopedEntity {
 private Long requestId;
 public Long getRequestId(){return requestId;} public void setRequestId(Long value){requestId=value;}
 private Long formulaItemId;
 public Long getFormulaItemId(){return formulaItemId;} public void setFormulaItemId(Long value){formulaItemId=value;}
 private Long prescriptionItemId;
 public Long getPrescriptionItemId(){return prescriptionItemId;} public void setPrescriptionItemId(Long value){prescriptionItemId=value;}
 public Long getRecipeItemId(){return prescriptionItemId!=null?prescriptionItemId:formulaItemId;}
 private Long materialId;
 public Long getMaterialId(){return materialId;} public void setMaterialId(Long value){materialId=value;}
 private java.math.BigDecimal requestedQty;
 public java.math.BigDecimal getRequestedQty(){return requestedQty;} public void setRequestedQty(java.math.BigDecimal value){requestedQty=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
}
