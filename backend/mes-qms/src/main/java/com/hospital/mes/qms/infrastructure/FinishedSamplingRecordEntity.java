package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("qms_finished_sampling_record") public class FinishedSamplingRecordEntity extends ScopedEntity {
 private Long inspectionRequestId;
 public Long getInspectionRequestId(){return inspectionRequestId;} public void setInspectionRequestId(Long value){inspectionRequestId=value;}
 private Long sampleId;
 public Long getSampleId(){return sampleId;} public void setSampleId(Long value){sampleId=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private String samplingNo;
 public String getSamplingNo(){return samplingNo;} public void setSamplingNo(String value){samplingNo=value;}
 private String samplingLocation;
 public String getSamplingLocation(){return samplingLocation;} public void setSamplingLocation(String value){samplingLocation=value;}
 private java.math.BigDecimal quantity;
 public java.math.BigDecimal getQuantity(){return quantity;} public void setQuantity(java.math.BigDecimal value){quantity=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private String samplingMethod;
 public String getSamplingMethod(){return samplingMethod;} public void setSamplingMethod(String value){samplingMethod=value;}
 private Long sampledBy;
 public Long getSampledBy(){return sampledBy;} public void setSampledBy(Long value){sampledBy=value;}
 private java.time.LocalDateTime sampledAt;
 public java.time.LocalDateTime getSampledAt(){return sampledAt;} public void setSampledAt(java.time.LocalDateTime value){sampledAt=value;}
 private Long signatureId;
 public Long getSignatureId(){return signatureId;} public void setSignatureId(Long value){signatureId=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
 private String reason;
 public String getReason(){return reason;} public void setReason(String value){reason=value;}
}
