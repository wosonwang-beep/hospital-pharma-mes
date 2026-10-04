package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.*;
import java.math.BigDecimal;
@TableName("wms_material_receipt")
public class ReceiptEntity extends ScopedEntity {
 private String receiptNo;
 public String getReceiptNo(){return receiptNo;} public void setReceiptNo(String value){receiptNo=value;}
 private Long supplierId;
 public Long getSupplierId(){return supplierId;} public void setSupplierId(Long value){supplierId=value;}
 private String purchaseOrderNo;
 public String getPurchaseOrderNo(){return purchaseOrderNo;} public void setPurchaseOrderNo(String value){purchaseOrderNo=value;}
 private String deliveryNoteNo;
 public String getDeliveryNoteNo(){return deliveryNoteNo;} public void setDeliveryNoteNo(String value){deliveryNoteNo=value;}
 private Long warehouseId;
 public Long getWarehouseId(){return warehouseId;} public void setWarehouseId(Long value){warehouseId=value;}
 private Boolean transportCheckPassed;
 public Boolean getTransportCheckPassed(){return transportCheckPassed;} public void setTransportCheckPassed(Boolean value){transportCheckPassed=value;}
 private String recordStatus;
 public String getRecordStatus(){return recordStatus;} public void setRecordStatus(String value){recordStatus=value;}
 private Long receivedBy;
 public Long getReceivedBy(){return receivedBy;} public void setReceivedBy(Long value){receivedBy=value;}
 private LocalDateTime receivedAt;
 public LocalDateTime getReceivedAt(){return receivedAt;} public void setReceivedAt(LocalDateTime value){receivedAt=value;}
 private Long confirmedBy;
 public Long getConfirmedBy(){return confirmedBy;} public void setConfirmedBy(Long value){confirmedBy=value;}
 private LocalDateTime confirmedAt;
 public LocalDateTime getConfirmedAt(){return confirmedAt;} public void setConfirmedAt(LocalDateTime value){confirmedAt=value;}
}
