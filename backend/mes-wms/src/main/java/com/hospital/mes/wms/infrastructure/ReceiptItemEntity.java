package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.*;
import java.math.BigDecimal;
@TableName("wms_material_receipt_item")
public class ReceiptItemEntity extends ScopedEntity {
 private Long receiptId;
 public Long getReceiptId(){return receiptId;} public void setReceiptId(Long value){receiptId=value;}
 private Long materialId;
 public Long getMaterialId(){return materialId;} public void setMaterialId(Long value){materialId=value;}
 private String lotNo;
 public String getLotNo(){return lotNo;} public void setLotNo(String value){lotNo=value;}
 private String supplierLotNo;
 public String getSupplierLotNo(){return supplierLotNo;} public void setSupplierLotNo(String value){supplierLotNo=value;}
 private String manufacturerLotNo;
 public String getManufacturerLotNo(){return manufacturerLotNo;} public void setManufacturerLotNo(String value){manufacturerLotNo=value;}
 private LocalDate manufactureDate;
 public LocalDate getManufactureDate(){return manufactureDate;} public void setManufactureDate(LocalDate value){manufactureDate=value;}
 private LocalDate expiryDate;
 public LocalDate getExpiryDate(){return expiryDate;} public void setExpiryDate(LocalDate value){expiryDate=value;}
 private LocalDate retestDate;
 public LocalDate getRetestDate(){return retestDate;} public void setRetestDate(LocalDate value){retestDate=value;}
 private BigDecimal receivedQty;
 public BigDecimal getReceivedQty(){return receivedQty;} public void setReceivedQty(BigDecimal value){receivedQty=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private String packageSpec;
 public String getPackageSpec(){return packageSpec;} public void setPackageSpec(String value){packageSpec=value;}
 private Integer packageCount;
 public Integer getPackageCount(){return packageCount;} public void setPackageCount(Integer value){packageCount=value;}
 private Long locationId;
 public Long getLocationId(){return locationId;} public void setLocationId(Long value){locationId=value;}
 private Long containerId;
 public Long getContainerId(){return containerId;} public void setContainerId(Long value){containerId=value;}
 private Boolean packageCheckPassed;
 public Boolean getPackageCheckPassed(){return packageCheckPassed;} public void setPackageCheckPassed(Boolean value){packageCheckPassed=value;}
 private Boolean sealCheckPassed;
 public Boolean getSealCheckPassed(){return sealCheckPassed;} public void setSealCheckPassed(Boolean value){sealCheckPassed=value;}
 private Boolean labelCheckPassed;
 public Boolean getLabelCheckPassed(){return labelCheckPassed;} public void setLabelCheckPassed(Boolean value){labelCheckPassed=value;}
 private Boolean damageCheckPassed;
 public Boolean getDamageCheckPassed(){return damageCheckPassed;} public void setDamageCheckPassed(Boolean value){damageCheckPassed=value;}
 private Boolean contaminationCheckPassed;
 public Boolean getContaminationCheckPassed(){return contaminationCheckPassed;} public void setContaminationCheckPassed(Boolean value){contaminationCheckPassed=value;}
 private String materialSnapshotJson;
 public String getMaterialSnapshotJson(){return materialSnapshotJson;} public void setMaterialSnapshotJson(String value){materialSnapshotJson=value;}
 private Boolean requiresIncomingInspectionSnapshot;
 public Boolean getRequiresIncomingInspectionSnapshot(){return requiresIncomingInspectionSnapshot;} public void setRequiresIncomingInspectionSnapshot(Boolean value){requiresIncomingInspectionSnapshot=value;}
}
