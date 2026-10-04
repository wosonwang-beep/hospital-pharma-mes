package com.hospital.mes.execution.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("mes_material_charge") public class ChargeEntity extends ScopedEntity {
 private Long executionUnitId;
 public Long getExecutionUnitId(){return executionUnitId;} public void setExecutionUnitId(Long value){executionUnitId=value;}
 private Long operationExecutionId;
 public Long getOperationExecutionId(){return operationExecutionId;} public void setOperationExecutionId(Long value){operationExecutionId=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Long weighingRecordId;
 public Long getWeighingRecordId(){return weighingRecordId;} public void setWeighingRecordId(Long value){weighingRecordId=value;}
 private BigDecimal chargedQty;
 public BigDecimal getChargedQty(){return chargedQty;} public void setChargedQty(BigDecimal value){chargedQty=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private Long chargedBy;
 public Long getChargedBy(){return chargedBy;} public void setChargedBy(Long value){chargedBy=value;}
 private Long verifiedBy;
 public Long getVerifiedBy(){return verifiedBy;} public void setVerifiedBy(Long value){verifiedBy=value;}
 private LocalDateTime chargedAt;
 public LocalDateTime getChargedAt(){return chargedAt;} public void setChargedAt(LocalDateTime value){chargedAt=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private String gateEvidenceJson;
 public String getGateEvidenceJson(){return gateEvidenceJson;} public void setGateEvidenceJson(String value){gateEvidenceJson=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
