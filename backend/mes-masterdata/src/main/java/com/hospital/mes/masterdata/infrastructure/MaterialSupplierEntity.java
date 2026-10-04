package com.hospital.mes.masterdata.infrastructure;
@com.baomidou.mybatisplus.annotation.TableName("md_material_supplier") public class MaterialSupplierEntity extends ScopedEntity {
 private Long materialId;
 public Long getMaterialId(){return materialId;} public void setMaterialId(Long value){materialId=value;}
 private Long supplierId;
 public Long getSupplierId(){return supplierId;} public void setSupplierId(Long value){supplierId=value;}
 private Boolean approved;
 public Boolean getApproved(){return approved;} public void setApproved(Boolean value){approved=value;}
 private java.time.LocalDate validTo;
 public java.time.LocalDate getValidTo(){return validTo;} public void setValidTo(java.time.LocalDate value){validTo=value;}

private Boolean preferred;
 public Boolean getPreferred(){return preferred;} public void setPreferred(Boolean value){preferred=value;}
}
