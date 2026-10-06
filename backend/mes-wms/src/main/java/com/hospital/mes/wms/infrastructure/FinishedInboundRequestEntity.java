package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("wms_finished_inbound_request") public class FinishedInboundRequestEntity extends ScopedEntity {
 private String requestNo;
 public String getRequestNo(){return requestNo;} public void setRequestNo(String value){requestNo=value;}
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Long productId;
 public Long getProductId(){return productId;} public void setProductId(Long value){productId=value;}
 private java.math.BigDecimal quantity;
 public java.math.BigDecimal getQuantity(){return quantity;} public void setQuantity(java.math.BigDecimal value){quantity=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private String sourceSnapshotJson;
 public String getSourceSnapshotJson(){return sourceSnapshotJson;} public void setSourceSnapshotJson(String value){sourceSnapshotJson=value;}
 private String sourceDigest;
 public String getSourceDigest(){return sourceDigest;} public void setSourceDigest(String value){sourceDigest=value;}
 private Long submittedBy;
 public Long getSubmittedBy(){return submittedBy;} public void setSubmittedBy(Long value){submittedBy=value;}
 private java.time.LocalDateTime submittedAt;
 public java.time.LocalDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(java.time.LocalDateTime value){submittedAt=value;}
 private Long confirmedBy;
 public Long getConfirmedBy(){return confirmedBy;} public void setConfirmedBy(Long value){confirmedBy=value;}
 private java.time.LocalDateTime confirmedAt;
 public java.time.LocalDateTime getConfirmedAt(){return confirmedAt;} public void setConfirmedAt(java.time.LocalDateTime value){confirmedAt=value;}
 private Long locationId;
 public Long getLocationId(){return locationId;} public void setLocationId(Long value){locationId=value;}
 private Long ledgerId;
 public Long getLedgerId(){return ledgerId;} public void setLedgerId(Long value){ledgerId=value;}
 private Long signatureId;
 public Long getSignatureId(){return signatureId;} public void setSignatureId(Long value){signatureId=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
 private String reason;
 public String getReason(){return reason;} public void setReason(String value){reason=value;}
}
