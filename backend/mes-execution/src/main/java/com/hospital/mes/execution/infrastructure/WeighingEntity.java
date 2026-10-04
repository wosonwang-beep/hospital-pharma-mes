package com.hospital.mes.execution.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("mes_weighing_record") public class WeighingEntity extends ScopedEntity {
 private Long executionUnitId;
 public Long getExecutionUnitId(){return executionUnitId;} public void setExecutionUnitId(Long value){executionUnitId=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Long bomItemId;
 public Long getBomItemId(){return bomItemId;} public void setBomItemId(Long value){bomItemId=value;}
 private BigDecimal targetQty;
 public BigDecimal getTargetQty(){return targetQty;} public void setTargetQty(BigDecimal value){targetQty=value;}
 private BigDecimal actualQty;
 public BigDecimal getActualQty(){return actualQty;} public void setActualQty(BigDecimal value){actualQty=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private Long weighedBy;
 public Long getWeighedBy(){return weighedBy;} public void setWeighedBy(Long value){weighedBy=value;}
 private Long verifiedBy;
 public Long getVerifiedBy(){return verifiedBy;} public void setVerifiedBy(Long value){verifiedBy=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private Long scaleEquipmentId;
 public Long getScaleEquipmentId(){return scaleEquipmentId;} public void setScaleEquipmentId(Long value){scaleEquipmentId=value;}
 private String equipmentEvidenceJson;
 public String getEquipmentEvidenceJson(){return equipmentEvidenceJson;} public void setEquipmentEvidenceJson(String value){equipmentEvidenceJson=value;}
 private String gateEvidenceJson;
 public String getGateEvidenceJson(){return gateEvidenceJson;} public void setGateEvidenceJson(String value){gateEvidenceJson=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
