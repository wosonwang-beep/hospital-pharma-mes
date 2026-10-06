package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("wms_finished_shipment") public class FinishedShipmentEntity extends ScopedEntity {
 private String shipmentNo;
 public String getShipmentNo(){return shipmentNo;} public void setShipmentNo(String value){shipmentNo=value;}
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Long locationId;
 public Long getLocationId(){return locationId;} public void setLocationId(Long value){locationId=value;}
 private java.math.BigDecimal quantity;
 public java.math.BigDecimal getQuantity(){return quantity;} public void setQuantity(java.math.BigDecimal value){quantity=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private String receivingParty;
 public String getReceivingParty(){return receivingParty;} public void setReceivingParty(String value){receivingParty=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private Long releaseDecisionId;
 public Long getReleaseDecisionId(){return releaseDecisionId;} public void setReleaseDecisionId(Long value){releaseDecisionId=value;}
 private Long ledgerId;
 public Long getLedgerId(){return ledgerId;} public void setLedgerId(Long value){ledgerId=value;}
 private Long confirmedBy;
 public Long getConfirmedBy(){return confirmedBy;} public void setConfirmedBy(Long value){confirmedBy=value;}
 private java.time.LocalDateTime confirmedAt;
 public java.time.LocalDateTime getConfirmedAt(){return confirmedAt;} public void setConfirmedAt(java.time.LocalDateTime value){confirmedAt=value;}
 private Long signatureId;
 public Long getSignatureId(){return signatureId;} public void setSignatureId(Long value){signatureId=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
 private String reason;
 public String getReason(){return reason;} public void setReason(String value){reason=value;}
}
