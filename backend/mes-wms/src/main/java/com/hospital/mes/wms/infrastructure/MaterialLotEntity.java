package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.*;
import java.math.BigDecimal;
@TableName("md_material_lot")
public class MaterialLotEntity extends ScopedEntity {
 private Long materialId;
 public Long getMaterialId(){return materialId;} public void setMaterialId(Long value){materialId=value;}
 private String lotNo;
 public String getLotNo(){return lotNo;} public void setLotNo(String value){lotNo=value;}
 private String supplierLotNo;
 public String getSupplierLotNo(){return supplierLotNo;} public void setSupplierLotNo(String value){supplierLotNo=value;}
 private LocalDate manufactureDate;
 public LocalDate getManufactureDate(){return manufactureDate;} public void setManufactureDate(LocalDate value){manufactureDate=value;}
 private LocalDate expiryDate;
 public LocalDate getExpiryDate(){return expiryDate;} public void setExpiryDate(LocalDate value){expiryDate=value;}
 private LocalDate retestDate;
 public LocalDate getRetestDate(){return retestDate;} public void setRetestDate(LocalDate value){retestDate=value;}
 private Long receiptItemId;
 public Long getReceiptItemId(){return receiptItemId;} public void setReceiptItemId(Long value){receiptItemId=value;}
 private String materialSnapshotJson;
 public String getMaterialSnapshotJson(){return materialSnapshotJson;} public void setMaterialSnapshotJson(String value){materialSnapshotJson=value;}
 private Boolean requiresIncomingInspectionSnapshot;
 public Boolean getRequiresIncomingInspectionSnapshot(){return requiresIncomingInspectionSnapshot;} public void setRequiresIncomingInspectionSnapshot(Boolean value){requiresIncomingInspectionSnapshot=value;}
 private String qualityStatus;
 public String getQualityStatus(){return qualityStatus;} public void setQualityStatus(String value){qualityStatus=value;}
 private String inventoryStatus;
 public String getInventoryStatus(){return inventoryStatus;} public void setInventoryStatus(String value){inventoryStatus=value;}
}
