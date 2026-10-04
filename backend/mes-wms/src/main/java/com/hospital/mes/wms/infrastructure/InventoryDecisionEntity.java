package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.LocalDateTime;
import java.math.BigDecimal;
@TableName(value="wms_material_lot_inventory_decision", excludeProperty={"updatedBy","updatedAt","versionNo"}) public class InventoryDecisionEntity extends ScopedEntity {
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Long previousDecisionId;
 public Long getPreviousDecisionId(){return previousDecisionId;} public void setPreviousDecisionId(Long value){previousDecisionId=value;}
 private String action;
 public String getAction(){return action;} public void setAction(String value){action=value;}
 private String previousInventoryStatus;
 public String getPreviousInventoryStatus(){return previousInventoryStatus;} public void setPreviousInventoryStatus(String value){previousInventoryStatus=value;}
 private String resultingInventoryStatus;
 public String getResultingInventoryStatus(){return resultingInventoryStatus;} public void setResultingInventoryStatus(String value){resultingInventoryStatus=value;}
 private String reason;
 public String getReason(){return reason;} public void setReason(String value){reason=value;}
 private Long decidedBy;
 public Long getDecidedBy(){return decidedBy;} public void setDecidedBy(Long value){decidedBy=value;}
 private LocalDateTime decidedAt;
 public LocalDateTime getDecidedAt(){return decidedAt;} public void setDecidedAt(LocalDateTime value){decidedAt=value;}
 private Long signatureId;
 public Long getSignatureId(){return signatureId;} public void setSignatureId(Long value){signatureId=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
