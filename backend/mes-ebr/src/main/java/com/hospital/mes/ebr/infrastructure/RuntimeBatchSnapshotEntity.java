package com.hospital.mes.ebr.infrastructure;
@com.baomidou.mybatisplus.annotation.TableName("ebr_batch_snapshot")
public class RuntimeBatchSnapshotEntity extends com.hospital.mes.masterdata.infrastructure.ScopedEntity {
 private Long mainBatchId; public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long v){mainBatchId=v;}
 private Long templateVersionId; public Long getTemplateVersionId(){return templateVersionId;} public void setTemplateVersionId(Long v){templateVersionId=v;}
 private String definitionHash; public String getDefinitionHash(){return definitionHash;} public void setDefinitionHash(String v){definitionHash=v;}
 private String snapshotJson; public String getSnapshotJson(){return snapshotJson;} public void setSnapshotJson(String v){snapshotJson=v;}
 private java.time.LocalDateTime frozenAt; public java.time.LocalDateTime getFrozenAt(){return frozenAt;} public void setFrozenAt(java.time.LocalDateTime v){frozenAt=v;}
}
