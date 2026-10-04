package com.hospital.mes.ebr.infrastructure;
@com.baomidou.mybatisplus.annotation.TableName("ebr_field_value_revision")
public class RuntimeValueEntity extends com.hospital.mes.masterdata.infrastructure.ScopedEntity {
 private Long formInstanceId; public Long getFormInstanceId(){return formInstanceId;} public void setFormInstanceId(Long v){formInstanceId=v;}
 private Long fieldDefId; public Long getFieldDefId(){return fieldDefId;} public void setFieldDefId(Long v){fieldDefId=v;}
 private Integer revisionNo; public Integer getRevisionNo(){return revisionNo;} public void setRevisionNo(Integer v){revisionNo=v;}
 private Long previousRevisionId; public Long getPreviousRevisionId(){return previousRevisionId;} public void setPreviousRevisionId(Long v){previousRevisionId=v;}
 private String rawValue; public String getRawValue(){return rawValue;} public void setRawValue(String v){rawValue=v;}
 private String derivedValue; public String getDerivedValue(){return derivedValue;} public void setDerivedValue(String v){derivedValue=v;}
 private String changeReason; public String getChangeReason(){return changeReason;} public void setChangeReason(String v){changeReason=v;}
 private java.time.LocalDateTime recordedAt; public java.time.LocalDateTime getRecordedAt(){return recordedAt;} public void setRecordedAt(java.time.LocalDateTime v){recordedAt=v;}
 private String fieldCode; public String getFieldCode(){return fieldCode;} public void setFieldCode(String v){fieldCode=v;}
 private String occurrencePath; public String getOccurrencePath(){return occurrencePath;} public void setOccurrencePath(String v){occurrencePath=v;}
 private String rawValueJson; public String getRawValueJson(){return rawValueJson;} public void setRawValueJson(String v){rawValueJson=v;}
 private String normalizedValueJson; public String getNormalizedValueJson(){return normalizedValueJson;} public void setNormalizedValueJson(String v){normalizedValueJson=v;}
 private Long unitId; public Long getUnitId(){return unitId;} public void setUnitId(Long v){unitId=v;}
 private String sourceType; public String getSourceType(){return sourceType;} public void setSourceType(String v){sourceType=v;}
 private String sourceRef; public String getSourceRef(){return sourceRef;} public void setSourceRef(String v){sourceRef=v;}
 private Long recordedBy; public Long getRecordedBy(){return recordedBy;} public void setRecordedBy(Long v){recordedBy=v;}
}
