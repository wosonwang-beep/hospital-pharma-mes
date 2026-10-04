package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_sample")
public class SampleRow extends MutableIncomingRow {
 private String sampleNo;
 public String getSampleNo(){return sampleNo;} public void setSampleNo(String value){sampleNo=value;}
 private String sampleScope;
 public String getSampleScope(){return sampleScope;} public void setSampleScope(String value){sampleScope=value;}
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Long samplingTaskId;
 public Long getSamplingTaskId(){return samplingTaskId;} public void setSamplingTaskId(Long value){samplingTaskId=value;}
 private Long samplingDetailId;
 public Long getSamplingDetailId(){return samplingDetailId;} public void setSamplingDetailId(Long value){samplingDetailId=value;}
 private Long inspectionRequestItemId;
 public Long getInspectionRequestItemId(){return inspectionRequestItemId;} public void setInspectionRequestItemId(Long value){inspectionRequestItemId=value;}
 private String sampleType;
 public String getSampleType(){return sampleType;} public void setSampleType(String value){sampleType=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private java.time.LocalDateTime sampledAt;
 public java.time.LocalDateTime getSampledAt(){return sampledAt;} public void setSampledAt(java.time.LocalDateTime value){sampledAt=value;}
 private java.math.BigDecimal quantity;
 public java.math.BigDecimal getQuantity(){return quantity;} public void setQuantity(java.math.BigDecimal value){quantity=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private Long receivedBy;
 public Long getReceivedBy(){return receivedBy;} public void setReceivedBy(Long value){receivedBy=value;}
 private java.time.LocalDateTime receivedAt;
 public java.time.LocalDateTime getReceivedAt(){return receivedAt;} public void setReceivedAt(java.time.LocalDateTime value){receivedAt=value;}
 private String storageLocation;
 public String getStorageLocation(){return storageLocation;} public void setStorageLocation(String value){storageLocation=value;}
 private Long disposalSignatureId;
 public Long getDisposalSignatureId(){return disposalSignatureId;} public void setDisposalSignatureId(Long value){disposalSignatureId=value;}
 private String disposalSignatureEvidenceJson;
 public String getDisposalSignatureEvidenceJson(){return disposalSignatureEvidenceJson;} public void setDisposalSignatureEvidenceJson(String value){disposalSignatureEvidenceJson=value;}
}
